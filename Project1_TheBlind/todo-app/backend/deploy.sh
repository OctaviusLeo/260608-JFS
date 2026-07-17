#!/bin/bash
# Builds the backend Docker image locally and deploys it to EC2 over SSH.
#
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh <EC2_USER> <EC2_HOST> <PATH_TO_PEM_KEY>
#
# Example:
#   ./deploy.sh ubuntu ec2-3-138-107-74.us-east-2.compute.amazonaws.com ../todo-app-key.pem
#
# Requirements:
#   1. Copy .env.example to .env and fill in real values
#   2. EC2 instance is running with port 22 open
#   3. Docker is installed on EC2

set -e

EC2_USER="${1:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
EC2_HOST="${2:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
PEM_KEY="${3:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"

IMAGE_NAME="theblind-backend"
TAR_FILE="backend.tar.gz"

if [ ! -f ".env" ]; then
    echo "ERROR: .env file not found. Copy .env.example to .env and fill in real values."
    exit 1
fi

# Load env vars from .env, skip comments and blank lines
set -a
source .env
set +a

echo "==> Step 1: Building the Docker image..."
# Gradle runs inside Docker now so no local Gradle daemon is needed
docker build -t "$IMAGE_NAME" .

echo "==> Step 2: Saving the image to a compressed file..."
docker save "$IMAGE_NAME" | gzip > "$TAR_FILE"
echo "    Saved to $TAR_FILE"

echo "==> Step 3: Uploading the image to EC2..."
scp -i "$PEM_KEY" -o StrictHostKeyChecking=no "$TAR_FILE" "$EC2_USER@$EC2_HOST:~/"
echo "    Upload complete."

echo "==> Step 4: Loading and starting the container on EC2..."
ssh -i "$PEM_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" bash << EOF
    set -e

    echo "--> Loading image..."
    docker load < ~/$TAR_FILE

    echo "--> Stopping existing container if running..."
    docker stop $IMAGE_NAME 2>/dev/null || true
    docker rm   $IMAGE_NAME 2>/dev/null || true

    echo "--> Creating data folder for SQLite..."
    # ~/todo-data on the host maps to /opt/app/data in the container
    mkdir -p ~/todo-data

    echo "--> Starting container..."
    docker run -d \
        --name $IMAGE_NAME \
        --restart unless-stopped \
        -p 8080:8080 \
        -v ~/todo-data:/opt/app/data \
        -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
        -e SECURITY_JWT_SECRET_KEY="$SECURITY_JWT_SECRET_KEY" \
        -e SECURITY_JWT_EXPIRATION_TIME="$SECURITY_JWT_EXPIRATION_TIME" \
        -e CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
        $IMAGE_NAME

    echo "--> Waiting for app to start..."
    sleep 10

    docker ps --filter "name=$IMAGE_NAME"
    docker logs --tail 20 $IMAGE_NAME
EOF

echo ""
echo "==> Done! Backend is at: http://$EC2_HOST:8080"

rm -f "$TAR_FILE"
echo "==> Cleaned up $TAR_FILE"
