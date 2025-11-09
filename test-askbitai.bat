@echo off
echo ============================================
echo === AskBit.AI End-to-End Test (Windows) ===
echo ============================================
echo.

echo 1. Testing health endpoint...
curl -s http://localhost:8080/api/v1/health
echo.
echo.

echo 2. Uploading test document...
curl -X POST http://localhost:8080/api/v1/documents/upload -F "file=@sample-docs/test-policy.pdf" -F "version=1.0" -F "description=Test Policy Document"
echo.
echo.

echo 3. Listing documents...
curl -s http://localhost:8080/api/v1/documents
echo.
echo.

echo ============================================
echo === Testing Specific Policy Questions ===
echo ============================================
echo.

echo 4. Question 1: What should I do if I'm feeling unwell but scheduled to work in the office?
curl -s -X POST http://localhost:8080/api/v1/ask -H "Content-Type: application/json" -d "{\"question\": \"What should I do if I'm feeling unwell but scheduled to work in the office?\"}"
echo.
echo.

echo 5. Question 2: When will Variable Pay be typically paid?
curl -s -X POST http://localhost:8080/api/v1/ask -H "Content-Type: application/json" -d "{\"question\": \"When will Variable Pay be typically paid?\"}"
echo.
echo.

echo 6. Question 3: How can employees maximize their Progressive Variable Pay?
curl -s -X POST http://localhost:8080/api/v1/ask -H "Content-Type: application/json" -d "{\"question\": \"How can employees maximize their Progressive Variable Pay?\"}"
echo.
echo.

echo 7. Question 4: How to track my compliance?
curl -s -X POST http://localhost:8080/api/v1/ask -H "Content-Type: application/json" -d "{\"question\": \"How to track my compliance?\"}"
echo.
echo.

echo ============================================
echo === Testing Cache Performance ===
echo ============================================
echo.

echo 8. Asking same question again (should be cached)...
curl -s -X POST http://localhost:8080/api/v1/ask -H "Content-Type: application/json" -d "{\"question\": \"What should I do if I'm feeling unwell but scheduled to work in the office?\"}"
echo.
echo.

echo ============================================
echo === Checking System Metrics ===
echo ============================================
echo.

echo 9. Retrieving system metrics...
curl -s http://localhost:8080/api/v1/admin/metrics
echo.
echo.

echo 10. Retrieving top questions...
curl -s http://localhost:8080/api/v1/admin/top-questions?limit=5
echo.
echo.

echo 11. Retrieving cache statistics...
curl -s http://localhost:8080/api/v1/admin/cache/stats
echo.
echo.

echo ============================================
echo === Test Complete ===
echo ============================================
pause

