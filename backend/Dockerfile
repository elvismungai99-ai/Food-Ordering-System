# Build stage with Eclipse Temurin JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# Production runtime stage with patched minimal JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-privileged user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
