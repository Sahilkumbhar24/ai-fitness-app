#!/bin/sh
set -e

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Optimized Single-Container Cloud Mode)     "
echo "============================================="

# Disable remote discovery & config polling (everything is local in this container)
export SPRING_CLOUD_CONFIG_ENABLED=false
export EUREKA_CLIENT_ENABLED=false

# Point Gateway routes directly to localhost microservices
export USER_SERVICE_URL=http://localhost:8081
export ACTIVITY_SERVICE_URL=http://localhost:8082
export AI_SERVICE_URL=http://localhost:8083

# Common low-memory JVM flags
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -Xss256k"

# 1. Start User Service (if jar exists)
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8081 -jar userservice.jar > /tmp/userservice.log 2>&1 &
fi

# 2. Start Activity Service (:8082)
echo "Starting Activity Service (:8082)..."
java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8082 -jar activityservice.jar > /tmp/activityservice.log 2>&1 &

# 3. Start AI Recommendation Service (:8083)
echo "Starting AI Recommendation Service (:8083)..."
java $JVM_COMMON -Xms32m -Xmx72m -XX:MaxMetaspaceSize=60m -Dserver.port=8083 -jar aiservice.jar > /tmp/aiservice.log 2>&1 &

# Brief pause for background services to initialize listeners
sleep 3

# 4. Start API Gateway in foreground (binds to Render $PORT)
echo "Starting API Gateway on port ${PORT:-8080}..."
exec java $JVM_COMMON -Xms48m -Xmx96m -XX:MaxMetaspaceSize=64m -Dserver.port=${PORT:-8080} -jar gateway.jar
