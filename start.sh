#!/bin/sh
set -e

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Fast Gateway-First Startup for Cloud)      "
echo "============================================="

# Disable remote discovery & config polling
export SPRING_CLOUD_CONFIG_ENABLED=false
export EUREKA_CLIENT_ENABLED=false

# Point Gateway routes directly to localhost microservices
export USER_SERVICE_URL=http://localhost:8081
export ACTIVITY_SERVICE_URL=http://localhost:8082
export AI_SERVICE_URL=http://localhost:8083

# Common low-memory JVM flags
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -Xss256k"

# 1. Start API Gateway FIRST so Render detects port 8080 immediately
echo "Starting API Gateway on port ${PORT:-8080} with dedicated CPU..."
java $JVM_COMMON -Xms48m -Xmx96m -XX:MaxMetaspaceSize=64m -Dserver.port=${PORT:-8080} -jar gateway.jar &
GATEWAY_PID=$!

# Wait for API Gateway port to open (typically ~25-30s with full CPU)
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

# 2. Now start backend microservices sequentially in the background
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8081 -jar userservice.jar > /tmp/userservice.log 2>&1 &
  sleep 2
fi

echo "Starting Activity Service (:8082)..."
java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8082 -jar activityservice.jar > /tmp/activityservice.log 2>&1 &
sleep 2

echo "Starting AI Recommendation Service (:8083)..."
java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8083 -jar aiservice.jar > /tmp/aiservice.log 2>&1 &

echo "All services initiated. Keeping Gateway active in foreground."

# Keep container alive by waiting for Gateway
wait $GATEWAY_PID
