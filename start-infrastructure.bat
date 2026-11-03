@echo off
TITLE AI Fitness Application - Infrastructure Launcher (Docker)
echo =================================================================
echo   STARTING INFRASTRUCTURE: POSTGRES, MONGO, KAFKA, KEYCLOAK
echo =================================================================
echo.

set DOCKER_CMD=docker
where docker >nul 2>nul
if %ERRORLEVEL% neq 0 (
    set DOCKER_CMD="C:\Users\HP\AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe"
)

echo Using Docker command: %DOCKER_CMD%
echo.
echo Launching containers in detached mode...
%DOCKER_CMD% compose -f "%~dp0docker-compose.yml" up -d

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Failed to start containers. Please ensure Docker Desktop is running!
    pause
    exit /b 1
)

echo.
echo =================================================================
echo   CONTAINERS STARTED SUCCESSFULLY!
echo =================================================================
echo   - PostgreSQL: localhost:5432 (Database: fitness-micro-user)
echo   - MongoDB:    localhost:27017
echo   - Kafka:      localhost:9092
echo   - ZooKeeper:  localhost:2181
echo   - Keycloak:   http://localhost:8181 (Realm: fitness-app)
echo                 Pre-created user: testuser / password123
echo =================================================================
echo.
echo Checking container status:
%DOCKER_CMD% compose -f "%~dp0docker-compose.yml" ps
echo.
pause
