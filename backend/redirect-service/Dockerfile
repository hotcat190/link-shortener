# Stage 1: Build the application
FROM maven:amazoncorretto AS build
WORKDIR /app

# 1. Copy the Parent POM (Root)
COPY pom.xml .

# 2. Copy the Common Module (POM + Source)
COPY common/pom.xml common/pom.xml
COPY common/src common/src

# 3. Copy the Creation Service (POM + Source)
COPY redirect-service/pom.xml redirect-service/pom.xml
COPY redirect-service/src redirect-service/src

# 4. Build everything
# -pl redirect-service : Only build the creation-service project
# -am : Also make (build) dependencies (which handles 'common' and 'parent')
RUN mvn clean package -DskipTests -pl redirect-service -am

# Stage 2: Run the application
FROM amazoncorretto:21
WORKDIR /app
# Copy the specific JAR from the target folder of the module
COPY --from=build /app/redirect-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]