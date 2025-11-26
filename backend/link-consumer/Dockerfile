# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Parent and Common
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY common/src common/src

# Copy Consumer
COPY link-consumer/pom.xml link-consumer/pom.xml
COPY link-consumer/src link-consumer/src

# Build
RUN mvn clean package -DskipTests -pl link-consumer -am

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/link-consumer/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]