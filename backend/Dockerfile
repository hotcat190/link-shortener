# Use the official OpenJDK image as the base image
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file to the container
COPY pom.xml .

# Copy the source code to the container
COPY src ./src

# Package the application using Maven
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS final
WORKDIR /app
COPY --from=build /app/target/linkshortener-0.0.1-SNAPSHOT.jar /app/app.jar

# Specify the command to run the application
CMD ["java", "-jar", "app.jar", "--cache.enabled=true"]