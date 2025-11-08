@echo off
REM Gradle Wrapper Initialization Script

echo ========================================
echo   Gradle Wrapper Setup
echo ========================================
echo.

echo Checking Gradle wrapper...
echo.

if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [WARNING] gradle-wrapper.jar not found
    echo Downloading Gradle wrapper...

    REM Use gradle wrapper command if gradle is installed
    where gradle >nul 2>&1
    if %errorlevel% equ 0 (
        echo Using installed Gradle to generate wrapper...
        gradle wrapper --gradle-version 8.5
    ) else (
        echo [ERROR] Gradle is not installed and wrapper jar is missing
        echo.
        echo Please do one of the following:
        echo 1. Install Gradle from https://gradle.org/install/
        echo 2. Download gradle-wrapper.jar manually
        echo.
        pause
        exit /b 1
    )
) else (
    echo [OK] gradle-wrapper.jar exists
)

if not exist "gradle\wrapper\gradle-wrapper.properties" (
    echo [ERROR] gradle-wrapper.properties not found
    pause
    exit /b 1
) else (
    echo [OK] gradle-wrapper.properties exists
)

if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found
    pause
    exit /b 1
) else (
    echo [OK] gradlew.bat exists
)

echo.
echo ========================================
echo Gradle wrapper is ready!
echo.
echo You can now run:
echo   gradlew.bat build
echo   gradlew.bat bootRun
echo ========================================
echo.

pause

