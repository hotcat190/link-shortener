# Use the official OpenJDK image as the base image
FROM maven:4.0.0-rc-5-amazoncorretto-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file to the container
COPY pom.xml .

# Copy the source code to the container
COPY src ./src

# Package the application using Maven
RUN mvn --version

RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
# Copy the specific JAR from the target folder of the module
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]