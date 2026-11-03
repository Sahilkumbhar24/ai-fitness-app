#!/bin/sh

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Fast Gateway-First Startup for Cloud)      "
echo "============================================="

# Disable remote discovery & config polling
export SPRING_CLOUD_CONFIG_ENABLED=false
export EUREKA_CLIENT_ENABLED=false

# Point microservice URLs directly to localhost
export USER_SERVICE_URL=http://localhost:8081
export ACTIVITY_SERVICE_URL=http://localhost:8082
export AI_SERVICE_URL=http://localhost:8083

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

# Common low-memory JVM flags strictly tuned for 512MB RAM container
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -Xss256k -XX:MaxMetaspaceSize=64m -XX:ReservedCodeCacheSize=16m"

# 1. Start API Gateway FIRST so Render detects port 8080 immediately
echo "Starting API Gateway on port ${PORT:-8080} with dedicated CPU..."
java $JVM_COMMON -Xms20m -Xmx44m -Dserver.port=${PORT:-8080} -jar gateway.jar &
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

# 2. Now start backend microservices in background with live logging
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java $JVM_COMMON -Xms20m -Xmx44m -Dserver.port=8081 -jar userservice.jar &
  sleep 2
fi

echo "Starting Activity Service (:8082)..."
java $JVM_COMMON -Xms20m -Xmx52m -Dserver.port=8082 -jar activityservice.jar &
sleep 2

echo "Starting AI Recommendation Service (:8083)..."
java $JVM_COMMON -Xms20m -Xmx44m -Dserver.port=8083 -jar aiservice.jar &

echo "All services initiated. Keeping Gateway active in foreground."

# Supervise Gateway to ensure container never unexpectedly terminates
while true; do
  if ! kill -0 $GATEWAY_PID 2>/dev/null; then
    echo "Gateway stopped! Restarting Gateway on port ${PORT:-8080}..."
    java $JVM_COMMON -Xms20m -Xmx44m -Dserver.port=${PORT:-8080} -jar gateway.jar &
    GATEWAY_PID=$!
  fi
  sleep 10
done
