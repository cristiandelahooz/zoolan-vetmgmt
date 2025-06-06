# Use a base image with JDK for building the project
FROM eclipse-temurin:21-jdk AS build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and configuration
COPY mvnw ./
COPY .mvn .mvn/

# Copy the Maven configuration file
COPY pom.xml .

# Download the necessary dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code and compile
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Use a lightweight base image for running the application
FROM eclipse-temurin:21-jre

# Set the working directory in the runtime image
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Define the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
