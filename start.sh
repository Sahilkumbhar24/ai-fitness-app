#!/bin/sh

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Optimized 3-JVM Architecture for Cloud)    "
echo "============================================="

# Disable remote discovery & config polling
export SPRING_CLOUD_CONFIG_ENABLED=false
export EUREKA_CLIENT_ENABLED=false

# Point microservice URLs directly to localhost:8082 (Activity Service is the unified high-performance microservice engine)
export USER_SERVICE_URL=http://localhost:8082
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

# Save the public port for API Gateway
PUBLIC_PORT=${PORT:-8080}

# 1. Start API Gateway FIRST on the public port so Render detects it immediately
echo "Starting API Gateway on public port ${PUBLIC_PORT}..."
java $JVM_COMMON -Xms24m -Xmx48m -XX:MaxMetaspaceSize=96m -Dserver.port=${PUBLIC_PORT} -jar gateway.jar &
GATEWAY_PID=$!

# Wait for API Gateway port to open
echo "Waiting for API Gateway on port ${PUBLIC_PORT}..."
RETRIES=90
while ! nc -z localhost ${PUBLIC_PORT} 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [ $RETRIES -le 0 ]; then
    echo "Gateway took longer than expected, proceeding with backend services..."
    break
  fi
  sleep 1
done

echo "API Gateway is active on port ${PUBLIC_PORT}!"

# Unset external PORT so internal microservices don't try to bind to the public port
unset PORT

# 2. Start Activity, AI & User Engine on port 8082 with self-healing restart supervision
start_activity_service() {
  while true; do
    echo "Starting Activity & AI Engine (:8082)..."
    ACTIVITY_SERVICE_PORT=8082 SERVER_PORT=8082 java $JVM_COMMON -Xms32m -Xmx112m -XX:MaxMetaspaceSize=120m -Dserver.port=8082 -jar activityservice.jar
    echo "Activity Engine process exited ($?), restarting in 2 seconds..."
    sleep 2
  done
}

start_activity_service &

# Wait for Activity Service to bind to port 8082
echo "Waiting for Activity & AI Engine on port 8082..."
RETRIES=60
while ! nc -z localhost 8082 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [ $RETRIES -le 0 ]; then
    echo "Activity Service startup in progress, proceeding..."
    break
  fi
  sleep 1
done

if nc -z localhost 8082 2>/dev/null; then
  echo "Activity & AI Engine is ACTIVE on port 8082!"
fi

# 3. Standalone services only started if explicitly opted in
if [ "${START_USER_SERVICE:-false}" = "true" ] && [ -f userservice.jar ]; then
  echo "Starting optional standalone User Service (:8081)..."
  USER_SERVICE_PORT=8081 SERVER_PORT=8081 java $JVM_COMMON -Xms16m -Xmx32m -Dserver.port=8081 -jar userservice.jar &
fi

if [ "${START_STANDALONE_AI:-false}" = "true" ] && [ -f aiservice.jar ]; then
  echo "Starting standalone AI Recommendation Service (:8083)..."
  AI_SERVICE_PORT=8083 SERVER_PORT=8083 java $JVM_COMMON -Xms24m -Xmx48m -Dserver.port=8083 -jar aiservice.jar &
fi

echo "All services initiated. Keeping Gateway active in foreground."

# Keep container alive by waiting for Gateway
wait $GATEWAY_PID
