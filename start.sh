#!/bin/sh
echo "Starting Eureka Discovery..."
java -Xmx96m -jar eureka.jar &
sleep 4

echo "Starting Activity Service..."
java -Xmx110m -jar activityservice.jar &

echo "Starting AI Recommendation Service..."
java -Xmx110m -jar aiservice.jar &
sleep 4

echo "Starting API Gateway on port ${PORT:-8080}..."
exec java -Xmx120m -Dserver.port=${PORT:-8080} -jar gateway.jar
