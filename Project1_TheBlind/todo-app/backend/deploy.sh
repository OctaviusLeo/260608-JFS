#!/bin/bash
# ============================================================
# deploy.sh — Build the backend Docker image locally and
# deploy it to an AWS EC2 instance.
#
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh <EC2_USER> <EC2_HOST> <PATH_TO_PEM_KEY>
#
# Example:
#   ./deploy.sh ec2-user 3.92.100.55 ~/.ssh/theblind-key.pem
#
# Before running:
#   1. Copy .env.example to .env and fill in real values.
#   2. Make sure the EC2 instance is running and port 22 is open.
#   3. Make sure Docker is installed on the EC2 instance.
# ============================================================

set -e  # stop the script immediately if any command fails

# --- Arguments ---
EC2_USER="${1:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
EC2_HOST="${2:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
PEM_KEY="${3:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"

IMAGE_NAME="theblind-backend"
TAR_FILE="backend.tar.gz"

# --- Load environment variables from .env ---
# We read these here so the script can pass them to the container at runtime.
if [ ! -f ".env" ]; then
    echo "ERROR: .env file not found. Copy .env.example to .env and fill in real values."
    exit 1
fi

# Export every line in .env that isn't a comment or blank
export $(grep -v '^#' .env | grep -v '^$' | xargs)

echo "==> Step 1: Building the Docker image locally..."
# The Dockerfile expects the fat JAR at build/libs/ so we need to run bootJar first.
# Run this from the backend directory (where this script lives).
./gradlew bootJar -x test
docker build -t "$IMAGE_NAME" .

echo "==> Step 2: Saving the image to a compressed archive..."
# docker save packages the full image; gzip shrinks it for faster transfer.
docker save "$IMAGE_NAME" | gzip > "$TAR_FILE"
echo "    Image saved to $TAR_FILE"

echo "==> Step 3: Copying the image to EC2..."
# scp transfers the file over SSH using the .pem key.
scp -i "$PEM_KEY" -o StrictHostKeyChecking=no "$TAR_FILE" "$EC2_USER@$EC2_HOST:~/"
echo "    Transfer complete."

echo "==> Step 4: Loading the image and running the container on EC2..."
# We pass all env vars inline so secrets never touch the EC2 filesystem as plain text.
# The volume ~/todo-data is where todo.db will live across container restarts.
ssh -i "$PEM_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" bash << EOF
    set -e

    echo "--> Loading Docker image..."
    docker load < ~/$TAR_FILE

    echo "--> Stopping and removing any existing container..."
    # If there's no existing container these commands will simply do nothing.
    docker stop $IMAGE_NAME 2>/dev/null || true
    docker rm   $IMAGE_NAME 2>/dev/null || true

    echo "--> Creating persistent data directory for SQLite..."
    # This is the host-side folder that maps to /opt/app/data inside the container.
    # todo.db writes here, so it survives container restarts and redeployments.
    mkdir -p ~/todo-data

    echo "--> Starting the backend container..."
    docker run -d \\
        --name $IMAGE_NAME \\
        --restart unless-stopped \\
        -p 8080:8080 \\
        -v ~/todo-data:/opt/app/data \\
        -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \\
        -e SECURITY_JWT_SECRET_KEY="$SECURITY_JWT_SECRET_KEY" \\
        -e SECURITY_JWT_EXPIRATION_TIME="$SECURITY_JWT_EXPIRATION_TIME" \\
        -e CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \\
        $IMAGE_NAME

    echo "--> Container started. Waiting 10 seconds for the app to boot..."
    sleep 10

    echo "--> Container status:"
    docker ps --filter "name=$IMAGE_NAME"

    echo "--> Last 20 lines of container logs:"
    docker logs --tail 20 $IMAGE_NAME
EOF

echo ""
echo "==> Deployment complete!"
echo "    Backend API is available at: http://$EC2_HOST:8080"
echo "    Quick smoke test:"
echo "      curl -s -o /dev/null -w '%{http_code}' http://$EC2_HOST:8080/api/register -X POST -H 'Content-Type: application/json' -d '{\"username\":\"test\",\"password\":\"test123\"}'"
echo ""
echo "    Share the EC2 public IP with your teammates:"
echo "      Dev 1 (Frontend): needs it to set the Angular apiUrl"
echo "      Dev 3 (CORS):     needs it to configure CORS_ALLOWED_ORIGINS"

# Clean up the local tar file — it's large and not needed anymore
rm -f "$TAR_FILE"
echo "==> Cleaned up local $TAR_FILE"
