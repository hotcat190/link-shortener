# Use the official OpenJDK image as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the Maven build file to the container
COPY pom.xml .

# Copy the source code to the container
COPY src ./src

# Package the application using Maven
RUN mvn package -DskipTests

# Expose the Nginx port, do not expose the application port 8080
EXPOSE 8000

# Specify the command to run the application
CMD ["java", "-jar", "target/linkshortener-0.0.1-SNAPSHOT.jar", "--cache.enabled=true"]