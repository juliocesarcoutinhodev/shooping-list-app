# syntax=docker/dockerfile:1.7

FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package \
    && JAR_FILE="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | sort | head -n 1)" \
    && test -n "${JAR_FILE}" \
    && cp "${JAR_FILE}" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

COPY --from=builder --chown=spring:spring /workspace/app.jar /app/app.jar

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
