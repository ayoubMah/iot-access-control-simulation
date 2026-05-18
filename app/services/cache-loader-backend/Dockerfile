# Build context expected: app/services/
# Build with: docker build -f cache-loader-backend/Dockerfile .

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY shared-model/pom.xml shared-model/
COPY cache-loader-backend/pom.xml cache-loader-backend/
RUN mvn -pl shared-model,cache-loader-backend -am -q -DskipTests dependency:go-offline

COPY shared-model/src shared-model/src
COPY cache-loader-backend/src cache-loader-backend/src
RUN mvn -pl cache-loader-backend -am -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/cache-loader-backend/target/cache-loader-backend-*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
