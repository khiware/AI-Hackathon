@echo off
REM AskBit.AI - Startup Script for Windows

echo ========================================
echo    AskBit.AI - Starting Application
echo ========================================
echo.

REM Check if OPENAI_API_KEY is set
if "%OPENAI_API_KEY%"=="" (
    echo [WARNING] OPENAI_API_KEY environment variable is not set!
    echo Please set it using: set OPENAI_API_KEY=your-api-key
    echo.
    echo You can also edit src\main\resources\application.properties
    echo.
    pause
    exit /b 1
)

echo [INFO] OPENAI_API_KEY is set
echo [INFO] Starting Spring Boot application...
echo.

REM Run the application
gradlew.bat bootRun

pause

