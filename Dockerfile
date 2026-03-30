# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the host machine into the container
COPY build/libs/wdyt-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
