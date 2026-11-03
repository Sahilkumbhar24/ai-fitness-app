#!/bin/sh
set -e

echo "============================================="
echo " Starting AI Fitness Application Backend     "
echo "============================================="

# Function to wait for a port to be open
wait_for_port() {
  local port=$1
  local name=$2
  local retries=30
  echo "Waiting for $name on port $port..."
  while ! nc -z localhost $port 2>/dev/null; do
    retries=$((retries - 1))
    if [ $retries -le 0 ]; then
      echo "Timed out waiting for $name on port $port, continuing anyway..."
      break
    fi
    sleep 1
  done
  echo "$name is ready!"
}

# 1. Start Eureka Discovery Registry
echo "Starting Eureka Discovery Registry (:8761)..."
java -Xms48m -Xmx80m -XX:+UseContainerSupport -jar eureka.jar > /tmp/eureka.log 2>&1 &
sleep 5

# 2. Start User Service (if jar exists)
if [ -f userservice.jar ]; then
  echo "Starting User Service (:8081)..."
  java -Xms64m -Xmx100m -XX:+UseContainerSupport -Dserver.port=8081 -jar userservice.jar > /tmp/userservice.log 2>&1 &
fi

# 3. Start Activity Service
echo "Starting Activity Service (:8082)..."
java -Xms64m -Xmx100m -XX:+UseContainerSupport -Dserver.port=8082 -jar activityservice.jar > /tmp/activityservice.log 2>&1 &

# 4. Start AI Recommendation Service
echo "Starting AI Recommendation Service (:8083)..."
java -Xms64m -Xmx100m -XX:+UseContainerSupport -Dserver.port=8083 -jar aiservice.jar > /tmp/aiservice.log 2>&1 &

# Wait briefly for services to register with Eureka
sleep 6

# 5. Start API Gateway in foreground (binds to Render $PORT)
echo "Starting API Gateway on port ${PORT:-8080}..."
exec java -Xms64m -Xmx110m -XX:+UseContainerSupport -Dserver.port=${PORT:-8080} -jar gateway.jar
