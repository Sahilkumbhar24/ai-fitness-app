#!/bin/sh
set -e

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo " (Optimized for 512MB Cloud Memory Limit)    "
echo "============================================="

# Common low-memory JVM flags for microservices on 512MB instances
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -Xss256k"

# 1. Start Eureka Discovery Registry
echo "Starting Eureka Discovery Registry (:8761)..."
java $JVM_COMMON -Xms32m -Xmx64m -XX:MaxMetaspaceSize=48m -jar eureka.jar > /tmp/eureka.log 2>&1 &
sleep 5

# 2. Start User Service (if jar exists)
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java $JVM_COMMON -Xms48m -Xmx80m -XX:MaxMetaspaceSize=64m -Dserver.port=8081 -jar userservice.jar > /tmp/userservice.log 2>&1 &
fi

# 3. Start Activity Service
echo "Starting Activity Service (:8082)..."
java $JVM_COMMON -Xms48m -Xmx80m -XX:MaxMetaspaceSize=64m -Dserver.port=8082 -jar activityservice.jar > /tmp/activityservice.log 2>&1 &

# 4. Start AI Recommendation Service
echo "Starting AI Recommendation Service (:8083)..."
java $JVM_COMMON -Xms48m -Xmx80m -XX:MaxMetaspaceSize=64m -Dserver.port=8083 -jar aiservice.jar > /tmp/aiservice.log 2>&1 &

# Wait briefly for services to register with Eureka
sleep 6

# 5. Start API Gateway in foreground (binds to Render $PORT)
echo "Starting API Gateway on port ${PORT:-8080}..."
exec java $JVM_COMMON -Xms48m -Xmx96m -XX:MaxMetaspaceSize=64m -Dserver.port=${PORT:-8080} -jar gateway.jar
