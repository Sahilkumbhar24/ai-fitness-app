@echo off
TITLE AI Fitness Application - Local Launcher
echo =================================================================
echo        AI FITNESS APPLICATION - LOCAL DEVELOPMENT LAUNCHER
echo =================================================================
echo.
echo Make sure PostgreSQL (5432), MongoDB (27017), Kafka (9092),
echo and Keycloak (8181) are already running!
echo.
pause

echo [1/6] Starting Eureka Discovery Server (:8761)...
start "Eureka Server" cmd /k "cd /d "%~dp0eureka" && mvnw.cmd spring-boot:run"
echo Waiting 15 seconds for Eureka to initialize...
timeout /t 15 /nobreak >nul

echo [2/6] Starting Config Server (:8888)...
start "Config Server" cmd /k "cd /d "%~dp0configserver" && mvnw.cmd spring-boot:run"
echo Waiting 10 seconds for Config Server to initialize...
timeout /t 10 /nobreak >nul

echo [3/6] Starting User Service (:8081)...
start "User Service" cmd /k "cd /d "%~dp0userservice" && mvnw.cmd spring-boot:run"

echo [4/6] Starting Activity Service (:8082)...
start "Activity Service" cmd /k "cd /d "%~dp0activityservice" && mvnw.cmd spring-boot:run"

echo [5/6] Starting AI Service (:8083)...
start "AI Service" cmd /k "cd /d "%~dp0aiservice" && mvnw.cmd spring-boot:run"
echo Waiting 8 seconds before starting Gateway...
timeout /t 8 /nobreak >nul

echo [6/6] Starting API Gateway (:8080)...
start "API Gateway" cmd /k "cd /d "%~dp0gateway" && mvnw.cmd spring-boot:run"

echo.
echo =================================================================
echo All backend services have been launched in separate windows!
echo Eureka Dashboard: http://localhost:8761
echo.
echo To run frontend:
echo   cd "fitness project"
echo   npm run dev
echo =================================================================
echo.
pause
