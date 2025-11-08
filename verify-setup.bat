@echo off
REM AskBit.AI - Setup Verification Script

echo ========================================
echo    AskBit.AI - Setup Verification
echo ========================================
echo.

echo Checking prerequisites...
echo.

REM Check Java version
echo [1/4] Checking Java installation...
java -version 2>&1 | findstr /i "version" >nul
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 17 or higher
    goto :end
) else (
    echo [OK] Java is installed:
    java -version 2>&1 | findstr /i "version"
)
echo.

REM Check if OPENAI_API_KEY is set
echo [2/4] Checking OPENAI_API_KEY...
if "%OPENAI_API_KEY%"=="" (
    echo [WARNING] OPENAI_API_KEY is not set
    echo You can set it using: set OPENAI_API_KEY=your-api-key
    echo Or edit src\main\resources\application.properties
) else (
    echo [OK] OPENAI_API_KEY is set
)
echo.

REM Check project structure
echo [3/4] Checking project structure...
if exist "build.gradle" (
    echo [OK] build.gradle found
) else (
    echo [ERROR] build.gradle not found
    goto :end
)

if exist "src\main\java\com\askbit\ai\AskBitAiApplication.java" (
    echo [OK] Main application class found
) else (
    echo [ERROR] Main application class not found
    goto :end
)

if exist "sample-docs" (
    echo [OK] Sample documents folder found
) else (
    echo [WARNING] Sample documents folder not found
)
echo.

REM Check Gradle
echo [4/4] Checking Gradle...
if exist "gradlew.bat" (
    echo [OK] Gradle wrapper found
) else (
    echo [ERROR] Gradle wrapper not found
    goto :end
)
echo.

echo ========================================
echo Setup verification complete!
echo.
echo Next steps:
echo 1. Set OPENAI_API_KEY if not already set
echo 2. Run: start.bat (to start the application)
echo 3. Run: demo.bat (to upload documents and test)
echo 4. Open: http://localhost:8080 (web UI)
echo ========================================
echo.

:end
pause

