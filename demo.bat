@echo off
REM AskBit.AI - Demo Script
REM This script uploads sample documents and tests the API

echo ========================================
echo    AskBit.AI - Demo Script
echo ========================================
echo.

set BASE_URL=http://localhost:8080/api/v1

echo [1/5] Checking if application is running...
curl -s %BASE_URL%/health >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Application is not running!
    echo Please start the application first using: start.bat
    pause
    exit /b 1
)
echo [OK] Application is running
echo.

echo [2/5] Uploading HR Policy document...
curl -X POST %BASE_URL%/documents/upload ^
  -F "file=@sample-docs\HR_Policy_v3.1.md" ^
  -F "version=3.1" ^
  -F "description=Human Resources Policy Document"
echo.
echo.

echo [3/5] Uploading IT Security Policy document...
curl -X POST %BASE_URL%/documents/upload ^
  -F "file=@sample-docs\IT_Security_Policy_v2.5.md" ^
  -F "version=2.5" ^
  -F "description=IT Security Policy Document"
echo.
echo.

echo [4/5] Uploading Company FAQ document...
curl -X POST %BASE_URL%/documents/upload ^
  -F "file=@sample-docs\Company_FAQ_v1.0.md" ^
  -F "version=1.0" ^
  -F "description=Company Frequently Asked Questions"
echo.
echo.

echo [5/5] Testing the /ask endpoint...
echo Question: "How many PTO days do full-time US employees get?"
curl -X POST %BASE_URL%/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"How many PTO days do full-time US employees get?\"}"
echo.
echo.

echo ========================================
echo Demo complete!
echo.
echo You can now:
echo - Open http://localhost:8080 in your browser
echo - Check metrics: curl %BASE_URL%/admin/metrics
echo - List documents: curl %BASE_URL%/documents
echo ========================================
echo.

pause

