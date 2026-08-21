#!/bin/sh
JVM_OPTS="-XX:+UseSerialGC -Xms32m -Xmx64m -XX:MaxMetaspaceSize=64m -Xss256k"

echo "Starting Activity Service..."
java $JVM_OPTS -jar activityservice.jar &

echo "Starting AI Recommendation Service..."
java $JVM_OPTS -jar aiservice.jar &
sleep 5

echo "Starting API Gateway on port ${PORT:-8080}..."
exec java $JVM_OPTS -Dserver.port=${PORT:-8080} -jar gateway.jar
