# Multi-stage Maven Build for all backend services
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and root POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy necessary microservice modules
COPY configserver configserver
COPY eureka eureka
COPY userservice userservice
COPY activityservice activityservice
COPY aiservice aiservice
COPY gateway gateway

# Make wrapper executable and package all JARs
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Production Alpine JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JARs from build stage
COPY --from=build /app/activityservice/target/*.jar activityservice.jar
COPY --from=build /app/aiservice/target/*.jar aiservice.jar
COPY --from=build /app/gateway/target/*.jar gateway.jar

# Copy startup script
COPY start.sh start.sh
RUN chmod +x start.sh

EXPOSE 8080
CMD ["./start.sh"]
