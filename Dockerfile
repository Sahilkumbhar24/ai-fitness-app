# Multi-stage Maven Build for all backend services
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and POMs
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY configserver/pom.xml configserver/
COPY eureka/pom.xml eureka/
COPY userservice/pom.xml userservice/
COPY activityservice/pom.xml activityservice/
COPY aiservice/pom.xml aiservice/
COPY gateway/pom.xml gateway/

# Copy all source code
COPY eureka/src eureka/src
COPY activityservice/src activityservice/src
COPY aiservice/src aiservice/src
COPY gateway/src gateway/src

# Package JARs (skip tests for fast build)
RUN ./mvnw clean package -DskipTests

# Production Alpine JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JARs from build stage
COPY --from=build /app/eureka/target/*.jar eureka.jar
COPY --from=build /app/activityservice/target/*.jar activityservice.jar
COPY --from=build /app/aiservice/target/*.jar aiservice.jar
COPY --from=build /app/gateway/target/*.jar gateway.jar

# Copy startup script
COPY start.sh start.sh
RUN chmod +x start.sh

EXPOSE 8080
CMD ["./start.sh"]
