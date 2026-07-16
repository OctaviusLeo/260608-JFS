#!/bin/bash
# This script builds the backend Docker image on your local machine
# and deploys it to the AWS EC2 instance over SSH.
#
# How to use it:
#   chmod +x deploy.sh
#   ./deploy.sh <EC2_USER> <EC2_HOST> <PATH_TO_PEM_KEY>
#
# Real example:
#   ./deploy.sh ec2-user ec2-3-138-107-74.us-east-2.compute.amazonaws.com ../todo-app-key.pem
#
# Before you run this make sure:
#   1. You copied .env.example to .env and filled in real values
#   2. The EC2 instance is running and port 22 is open to your IP
#   3. Docker is installed on the EC2 instance
#   Note: you no longer need to run Gradle locally — the Dockerfile compiles the app itself

set -e  # if any command fails the whole script stops

# Read the three arguments passed in when running the script
EC2_USER="${1:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
EC2_HOST="${2:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"
PEM_KEY="${3:?Usage: ./deploy.sh <EC2_USER> <EC2_HOST> <PEM_KEY_PATH>}"

IMAGE_NAME="theblind-backend"
TAR_FILE="backend.tar.gz"

# Load the environment variables from the .env file so we can pass them to the container
if [ ! -f ".env" ]; then
    echo "ERROR: .env file not found. Copy .env.example to .env and fill in real values."
    exit 1
fi

# Pull in every variable from .env that is not a comment or empty line
export $(grep -v '^#' .env | grep -v '^$' | xargs)

echo "==> Step 1: Building the Docker image locally..."
# The Dockerfile now compiles the app inside Docker using the Gradle build stage
# so there is no need to run Gradle locally at all — this avoids daemon crashes on Windows
docker build -t "$IMAGE_NAME" .

echo "==> Step 2: Saving the image to a compressed file..."
# docker save bundles the whole image and gzip makes it smaller for the upload
docker save "$IMAGE_NAME" | gzip > "$TAR_FILE"
echo "    Saved to $TAR_FILE"

echo "==> Step 3: Uploading the image to EC2..."
# scp copies the file to the EC2 home directory using the pem key for authentication
scp -i "$PEM_KEY" -o StrictHostKeyChecking=no "$TAR_FILE" "$EC2_USER@$EC2_HOST:~/"
echo "    Upload complete."

echo "==> Step 4: Loading the image and starting the container on EC2..."
# All env vars are passed directly into the run command so secrets never sit on the EC2 disk
# The folder ~/todo-data on EC2 is where todo.db lives and persists across restarts
ssh -i "$PEM_KEY" -o StrictHostKeyChecking=no "$EC2_USER@$EC2_HOST" bash << EOF
    set -e

    echo "--> Loading Docker image..."
    docker load < ~/$TAR_FILE

    echo "--> Stopping any container that is already running..."
    # These two lines do nothing if there is no existing container to stop
    docker stop $IMAGE_NAME 2>/dev/null || true
    docker rm   $IMAGE_NAME 2>/dev/null || true

    echo "--> Setting up the data folder for SQLite..."
    # This folder on the EC2 host maps to /opt/app/data inside the container
    # The database file todo.db goes here and stays even after the container is replaced
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

    echo "--> Waiting 10 seconds for the app to finish starting up..."
    sleep 10

    echo "--> Container status:"
    docker ps --filter "name=$IMAGE_NAME"

    echo "--> Last 20 lines of logs:"
    docker logs --tail 20 $IMAGE_NAME
EOF

echo ""
echo "==> Deployment complete!"
echo "    Backend is running at: http://$EC2_HOST:8080"
echo "    Quick smoke test:"
echo "      curl -s -o /dev/null -w '%{http_code}' http://$EC2_HOST:8080/api/register -X POST -H 'Content-Type: application/json' -d '{\"username\":\"test\",\"password\":\"test123\"}'"
echo ""
echo "    Remind your teammates:"
echo "      Dev 1 (Frontend) needs the EC2 address to set the Angular apiUrl"
echo "      Dev 3 (CORS) needs the EC2 address and the S3 URL for WebConfig.java"

# Remove the local tar file since we do not need it anymore
rm -f "$TAR_FILE"
echo "==> Removed local $TAR_FILE"
