#!/bin/bash

echo "============================================"
echo "=== AskBit.AI End-to-End Test ==="
echo "============================================"
echo ""

echo "1. Testing health endpoint..."
curl -s http://localhost:8080/api/v1/health
echo -e "\n"

echo "2. Uploading test document..."
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@sample-docs/test-policy.pdf" \
  -F "version=1.0" \
  -F "description=Test Policy Document"
echo -e "\n"

echo "3. Listing documents..."
curl -s http://localhost:8080/api/v1/documents | json_pp
echo -e "\n"

echo "============================================"
echo "=== Testing Specific Policy Questions ==="
echo "============================================"
echo ""

echo "4. Question 1: What should I do if I'm feeling unwell but scheduled to work in the office?"
curl -s -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What should I do if I am feeling unwell but scheduled to work in the office?"}' | json_pp
echo -e "\n"

echo "5. Question 2: When will Variable Pay be typically paid?"
curl -s -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "When will Variable Pay be typically paid?"}' | json_pp
echo -e "\n"

echo "6. Question 3: How can employees maximize their Progressive Variable Pay?"
curl -s -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How can employees maximize their Progressive Variable Pay?"}' | json_pp
echo -e "\n"

echo "7. Question 4: How to track my compliance?"
curl -s -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How to track my compliance?"}' | json_pp
echo -e "\n"

echo "============================================"
echo "=== Testing Cache Performance ==="
echo "============================================"
echo ""

echo "8. Asking same question again (should be cached)..."
curl -s -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What should I do if I am feeling unwell but scheduled to work in the office?"}' | json_pp
echo -e "\n"

echo "============================================"
echo "=== Checking System Metrics ==="
echo "============================================"
echo ""

echo "9. Retrieving system metrics..."
curl -s http://localhost:8080/api/v1/admin/metrics | json_pp
echo -e "\n"

echo "10. Retrieving top questions..."
curl -s http://localhost:8080/api/v1/admin/top-questions?limit=5 | json_pp
echo -e "\n"

echo "11. Retrieving cache statistics..."
curl -s http://localhost:8080/api/v1/admin/cache/stats | json_pp
echo -e "\n"

echo "============================================"
echo "=== Test Complete ==="
echo "============================================"

