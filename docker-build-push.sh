#!/bin/bash

# Set the tag from the first parameter or default to 'latest'
# ====== CONFIG ======
TAG=${1:-latest}                   # Image version tag
AWS_REGION="us-east-1"             # AWS region
AWS_PROFILE="holo-cli"             # AWS CLI profile
ACCOUNT_ID="976193257823"
REPO_NAME="wdyt-service"
ECR_URL="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME"
# =====================

# Step 1: Build the Gradle project
./gradlew build
if [ $? -ne 0 ]; then
  echo "Gradle build failed. Exiting."
  exit 1
fi

# Step 2: Build the Docker image for the specified platform
docker build --platform linux/amd64 -t $REPO_NAME .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi

# Step 3: Login to AWS ECR using the specified profile and region
aws ecr get-login-password --region $AWS_REGION --profile $AWS_PROFILE | \
    docker login --username AWS --password-stdin $ECR_URL
if [ $? -ne 0 ]; then
  echo "Docker login to AWS ECR failed. Exiting."
  exit 1
fi

# Step 4: Tag the Docker image
docker tag $REPO_NAME:latest $ECR_URL:$TAG
if [ $? -ne 0 ]; then
  echo "Docker tagging failed. Exiting."
  exit 1
fi

# Step 5: Push the Docker image to AWS ECR
docker push $ECR_URL:$TAG
if [ $? -ne 0 ]; then
  echo "Docker push failed. Exiting."
  exit 1
fi

# Success message
echo "Docker image pushed successfully with tag: $TAG"
