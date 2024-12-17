#!/bin/bash

# Set the tag from the first parameter or default to 'latest'
TAG=${1:-latest}

# Step 1: Build the Gradle project
./gradlew build
if [ $? -ne 0 ]; then
  echo "Gradle build failed. Exiting."
  exit 1
fi

# Step 2: Build the Docker image for the specified platform
docker build --platform linux/amd64 -t wdyt-service .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi

# Step 3: Login to AWS ECR using the specified profile and region
aws ecr get-login-password --region us-east-1 --profile fr-cli | docker login --username AWS --password-stdin 071094189941.dkr.ecr.us-east-1.amazonaws.com
if [ $? -ne 0 ]; then
  echo "Docker login to AWS ECR failed. Exiting."
  exit 1
fi

# Step 4: Tag the Docker image
docker tag wdyt-service:latest 071094189941.dkr.ecr.us-east-1.amazonaws.com/wdyt:$TAG
if [ $? -ne 0 ]; then
  echo "Docker tagging failed. Exiting."
  exit 1
fi

# Step 5: Push the Docker image to AWS ECR
docker push 071094189941.dkr.ecr.us-east-1.amazonaws.com/wdyt:$TAG
if [ $? -ne 0 ]; then
  echo "Docker push failed. Exiting."
  exit 1
fi

# Success message
echo "Docker image pushed successfully with tag: $TAG"
