# --- Stage 1: Build the application with Maven ---
# Use a standard Maven image that includes JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching.
# Dependencies are only re-downloaded if pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping the tests for a faster build.
# The result will be a .jar file in the /app/target/ directory.
RUN mvn package -DskipTests


# --- Stage 2: Create the final, lightweight image ---
# Use a JRE-only image based on Alpine for a smaller footprint
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the compiled .jar file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# This is the command that will run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]