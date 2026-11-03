@echo off
TITLE AI Fitness Application - Stop Infrastructure (Docker)
echo Stopping all infrastructure containers...

set DOCKER_CMD=docker
where docker >nul 2>nul
if %ERRORLEVEL% neq 0 (
    set DOCKER_CMD="C:\Users\HP\AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe"
)

%DOCKER_CMD% compose -f "%~dp0docker-compose.yml" down
echo Containers stopped.
pause
