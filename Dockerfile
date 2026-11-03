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

# Production Alpine JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install netcat and curl for networking checks
RUN apk add --no-cache netcat-openbsd curl

# Copy built JARs from build stage
COPY --from=build /app/eureka/target/*.jar eureka.jar
COPY --from=build /app/userservice/target/*.jar userservice.jar
COPY --from=build /app/activityservice/target/*.jar activityservice.jar
COPY --from=build /app/aiservice/target/*.jar aiservice.jar
COPY --from=build /app/gateway/target/*.jar gateway.jar

# Copy startup script
COPY start.sh start.sh
RUN sed -i 's/\r$//' start.sh && chmod +x start.sh

EXPOSE 8080
CMD ["./start.sh"]
