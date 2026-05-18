# Build context expected: app/services/
# Build with: docker build -f core-operational-backend/Dockerfile .

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY shared-model/pom.xml shared-model/
COPY core-operational-backend/pom.xml core-operational-backend/
RUN mvn -pl shared-model,core-operational-backend -am -q -DskipTests dependency:go-offline

COPY shared-model/src shared-model/src
COPY core-operational-backend/src core-operational-backend/src
RUN mvn -pl core-operational-backend -am -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /workspace/core-operational-backend/target/core-operational-backend-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
