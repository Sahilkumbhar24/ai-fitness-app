# Multi-stage Maven Build for all backend services
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and root POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy all microservice modules
COPY configserver configserver
COPY eureka eureka
COPY userservice userservice
COPY activityservice activityservice
COPY aiservice aiservice
COPY gateway gateway

# Fix line endings and package all JARs
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package -DskipTests -B

# Stage the built JARs cleanly
RUN mkdir -p /jars && \
    find /app/eureka/target -maxdepth 1 -name "*.jar" ! -name "*original*" ! -name "*-sources*" -exec cp {} /jars/eureka.jar \; && \
    find /app/userservice/target -maxdepth 1 -name "*.jar" ! -name "*original*" ! -name "*-sources*" -exec cp {} /jars/userservice.jar \; && \
    find /app/activityservice/target -maxdepth 1 -name "*.jar" ! -name "*original*" ! -name "*-sources*" -exec cp {} /jars/activityservice.jar \; && \
    find /app/aiservice/target -maxdepth 1 -name "*.jar" ! -name "*original*" ! -name "*-sources*" -exec cp {} /jars/aiservice.jar \; && \
    find /app/gateway/target -maxdepth 1 -name "*.jar" ! -name "*original*" ! -name "*-sources*" -exec cp {} /jars/gateway.jar \;

# Production Alpine JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install netcat, curl, and dos2unix for networking and reliable script execution
RUN apk add --no-cache netcat-openbsd curl dos2unix

# Copy built JARs from staging
COPY --from=build /jars/*.jar /app/

# Copy startup script and guarantee Unix line endings
COPY start.sh start.sh
RUN dos2unix start.sh && chmod +x start.sh

EXPOSE 8080
CMD ["/bin/sh", "start.sh"]
