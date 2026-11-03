#!/bin/sh

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Optimized 3-JVM Architecture for Cloud)    "
echo "============================================="

# Disable remote discovery & config polling
export SPRING_CLOUD_CONFIG_ENABLED=false
export EUREKA_CLIENT_ENABLED=false

# Point microservice URLs directly to localhost (Activity Service handles both activities and AI recommendations)
export USER_SERVICE_URL=http://localhost:8081
export ACTIVITY_SERVICE_URL=http://localhost:8082
export AI_SERVICE_URL=http://localhost:8082

# Fallback Keycloak JWK URI if not explicitly set
export KEYCLOAK_JWK_URI="${KEYCLOAK_JWK_URI:-https://fitness-keycloak-ho08.onrender.com/realms/fitness-app/protocol/openid-connect/certs}"

# Sanitize Render PostgreSQL connection string for Spring JDBC if needed
if [ -n "$SPRING_DATASOURCE_URL" ]; then
  case "$SPRING_DATASOURCE_URL" in
    jdbc:postgresql:*)
      ;;
    postgres://*@*|postgresql://*@*)
      CLEAN_HOST_PATH="${SPRING_DATASOURCE_URL#*@}"
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${CLEAN_HOST_PATH}"
      ;;
    postgres://*)
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${SPRING_DATASOURCE_URL#postgres://}"
      ;;
    postgresql://*)
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${SPRING_DATASOURCE_URL#postgresql://}"
      ;;
  esac

  case "$SPRING_DATASOURCE_URL" in
    *sslmode=*) ;;
    *render.com*)
      case "$SPRING_DATASOURCE_URL" in
        *\?*) export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}&sslmode=require" ;;
        *)    export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}?sslmode=require" ;;
      esac
      ;;
    *)
      # For Render internal postgres network (dpg-xxxx), use sslmode=prefer so it connects cleanly
      case "$SPRING_DATASOURCE_URL" in
        *\?*) export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}&sslmode=prefer" ;;
        *)    export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}?sslmode=prefer" ;;
      esac
      ;;
  esac

  echo "Using SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}"
fi

# Common low-memory JVM flags
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -Xss256k"

# 1. Start API Gateway FIRST so Render detects port 8080 immediately
echo "Starting API Gateway on port ${PORT:-8080}..."
java $JVM_COMMON -Xms32m -Xmx64m -Dserver.port=${PORT:-8080} -jar gateway.jar &
GATEWAY_PID=$!

# Wait for API Gateway port to open
echo "Waiting for API Gateway on port ${PORT:-8080}..."
RETRIES=90
while ! nc -z localhost ${PORT:-8080} 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [ $RETRIES -le 0 ]; then
    echo "Gateway took longer than expected, proceeding with backend services..."
    break
  fi
  sleep 1
done

echo "API Gateway is active on port ${PORT:-8080}!"

# 2. Start backend microservices
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java $JVM_COMMON -Xms24m -Xmx48m -Dserver.port=8081 -jar userservice.jar &
  sleep 2
fi

echo "Starting Activity & AI Service (:8082)..."
java $JVM_COMMON -Xms32m -Xmx96m -Dserver.port=8082 -jar activityservice.jar &

# Optional: Only run standalone AI service if explicitly enabled
if [ "${START_STANDALONE_AI:-false}" = "true" ] && [ -f aiservice.jar ]; then
  echo "Starting standalone AI Recommendation Service (:8083)..."
  java $JVM_COMMON -Xms24m -Xmx48m -Dserver.port=8083 -jar aiservice.jar &
fi

echo "All services initiated. Keeping Gateway active in foreground."

# Keep container alive by waiting for Gateway
wait $GATEWAY_PID
