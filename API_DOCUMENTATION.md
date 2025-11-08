K# AskBit.AI - API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

---

## Endpoints

### 1. Ask a Question

**POST** `/ask`

Ask a question about company policies and get an AI-generated answer with citations.

#### Request Body
```json
{
  "question": "Can I expense a laptop if I'm a contractor in Germany?",
  "conversationId": "optional-conversation-id",
  "context": "optional-additional-context"
}
```

#### Response
```json
{
  "answer": "Yes, contractors in Germany can expense laptops up to €800.",
  "citations": [
    {
      "documentId": "abc12345",
      "fileName": "HR_Policy_v3.1.pdf",
      "version": "3.1",
      "pageNumber": 12,
      "section": "§4.2",
      "startLine": 14,
      "endLine": 18,
      "snippet": "Contractors in Germany are eligible to expense equipment...",
      "relevanceScore": 0.92
    }
  ],
  "confidence": 0.92,
  "cached": false,
  "needsClarification": false,
  "clarificationQuestion": null,
  "responseTimeMs": 1234,
  "modelUsed": "openai-gpt4",
  "piiRedacted": false
}
```

#### Status Codes
- `200 OK` - Question answered successfully
- `500 Internal Server Error` - Error processing question

#### Example with cURL (Windows)
```bash
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"What is the PTO policy for full-time employees?\"}"
```

#### Example Response - No Information Found
```json
{
  "answer": "I couldn't find a clear policy on this. Please check with HR or submit a ticket.",
  "citations": [],
  "confidence": 0.0,
  "cached": false,
  "needsClarification": false,
  "responseTimeMs": 234
}
```

#### Example Response - Needs Clarification
```json
{
  "answer": null,
  "citations": [],
  "confidence": null,
  "cached": false,
  "needsClarification": true,
  "clarificationQuestion": "I found information about 'PTO', but I need more context. Are you asking about:\n1. full-time employees in the US\n2. part-time employees\n3. contractors\n4. international employees\n\nPlease specify which one you're interested in.",
  "responseTimeMs": 123
}
```

---

### 2. Upload Document

**POST** `/documents/upload`

Upload and process a policy document (PDF, DOCX, Markdown, or text file).

#### Request (Multipart Form Data)
- `file` (required) - The document file
- `version` (optional) - Document version (e.g., "3.1", default: "1.0")
- `description` (optional) - Document description

#### Response
```json
{
  "documentId": "abc12345",
  "fileName": "HR_Policy_v3.1.pdf",
  "version": "3.1",
  "success": true,
  "message": "Document uploaded and processed successfully",
  "pagesProcessed": 24,
  "chunksCreated": 67
}
```

#### Status Codes
- `200 OK` - Document uploaded and processed successfully
- `500 Internal Server Error` - Error processing document

#### Example with cURL (Windows)
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@C:\path\to\HR_Policy.pdf" ^
  -F "version=3.1" ^
  -F "description=Human Resources Policy Document"
```

---

### 3. Get All Documents

**GET** `/documents`

Retrieve a list of all uploaded documents.

#### Response
```json
[
  {
    "id": 1,
    "fileName": "HR_Policy_v3.1.pdf",
    "documentId": "abc12345",
    "version": "3.1",
    "fileType": "pdf",
    "filePath": "./documents/abc12345_HR_Policy_v3.1.pdf",
    "description": "Human Resources Policy Document",
    "uploadedAt": "2025-11-08T10:30:00",
    "lastModifiedAt": "2025-11-08T10:30:00",
    "active": true,
    "pageCount": 24,
    "fileSize": 524288,
    "indexed": true
  }
]
```

#### Example with cURL
```bash
curl http://localhost:8080/api/v1/documents
```

---

### 4. Get Document by ID

**GET** `/documents/{documentId}`

Retrieve details of a specific document.

#### Path Parameters
- `documentId` - The document ID

#### Response
```json
{
  "id": 1,
  "fileName": "HR_Policy_v3.1.pdf",
  "documentId": "abc12345",
  "version": "3.1",
  "fileType": "pdf",
  "filePath": "./documents/abc12345_HR_Policy_v3.1.pdf",
  "description": "Human Resources Policy Document",
  "uploadedAt": "2025-11-08T10:30:00",
  "lastModifiedAt": "2025-11-08T10:30:00",
  "active": true,
  "pageCount": 24,
  "fileSize": 524288,
  "indexed": true
}
```

#### Status Codes
- `200 OK` - Document found
- `404 Not Found` - Document not found

#### Example with cURL
```bash
curl http://localhost:8080/api/v1/documents/abc12345
```

---

### 5. Delete Document

**DELETE** `/documents/{documentId}`

Soft delete a document (marks as inactive, doesn't physically delete).

#### Path Parameters
- `documentId` - The document ID

#### Status Codes
- `200 OK` - Document deleted successfully
- `404 Not Found` - Document not found

#### Example with cURL
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/abc12345
```

---

### 6. Get System Metrics

**GET** `/admin/metrics`

Retrieve system performance and usage metrics.

#### Response
```json
{
  "totalQueries": 1547,
  "averageResponseTimeMs": 1234.5,
  "cacheHitRate": 34.2,
  "totalDocuments": 12,
  "totalChunks": 456,
  "averageConfidence": 0.87,
  "piiRedactionCount": 23,
  "clarificationCount": 45,
  "mostUsedModel": "openai-gpt4"
}
```

#### Example with cURL
```bash
curl http://localhost:8080/api/v1/admin/metrics
```

---

### 7. Health Check

**GET** `/health`

Simple health check endpoint.

#### Response
```
AskBit.AI is running
```

#### Example with cURL
```bash
curl http://localhost:8080/api/v1/health
```

---

## Complete Usage Example

### Scenario: Employee wants to know about PTO policy

#### Step 1: Upload HR Policy Document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@HR_Policy.pdf" ^
  -F "version=3.1"
```

#### Step 2: Wait for processing (check response)
Response will show `"indexed": true` when ready.

#### Step 3: Ask Question
```bash
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"How many PTO days do full-time US employees get?\"}"
```

#### Step 4: Get Answer with Citations
```json
{
  "answer": "Full-time employees in the United States are entitled to 20 days of paid time off per year, plus 10 sick days and 5 personal days.",
  "citations": [
    {
      "documentId": "abc12345",
      "fileName": "HR_Policy_v3.1.pdf",
      "version": "3.1",
      "pageNumber": 2,
      "snippet": "Full-time employees in the United States are entitled to: 20 days of paid time off per year...",
      "relevanceScore": 0.95
    }
  ],
  "confidence": 0.95,
  "cached": false,
  "responseTimeMs": 1456,
  "modelUsed": "openai-gpt4"
}
```

#### Step 5: Ask Same Question Again (Cache Hit)
```bash
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"How many PTO days do full-time US employees get?\"}"
```

Response:
```json
{
  "answer": "Full-time employees in the United States are entitled to 20 days of paid time off per year, plus 10 sick days and 5 personal days.",
  "citations": [...],
  "confidence": 0.95,
  "cached": true,
  "responseTimeMs": 45,
  "modelUsed": "cached"
}
```

Note: Response time drops from 1456ms to 45ms! ⚡

---

## Error Handling

### Common Error Responses

#### Invalid Request
```json
{
  "timestamp": "2025-11-08T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Question cannot be empty",
  "path": "/api/v1/ask"
}
```

#### Document Not Found
```json
{
  "timestamp": "2025-11-08T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Document not found: abc12345",
  "path": "/api/v1/documents/abc12345"
}
```

#### Server Error
```json
{
  "timestamp": "2025-11-08T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An error occurred while processing your question. Please try again.",
  "path": "/api/v1/ask"
}
```

---

## Rate Limiting

Currently no rate limiting is implemented. In production, consider:
- 100 requests per minute per IP
- 1000 requests per hour per user
- Different limits for document upload vs. queries

---

## Authentication

Currently no authentication is implemented. For production deployment, consider:
- JWT tokens
- OAuth 2.0
- API keys
- Role-based access control (RBAC)

---

## Best Practices

### 1. Question Formatting
✅ **Good**: "What is the PTO policy for full-time employees in the US?"
❌ **Too vague**: "What's the policy?"

### 2. Document Upload
- Upload documents in order of importance
- Use clear, descriptive filenames
- Include version numbers
- Wait for indexing to complete before querying

### 3. Caching
- Cache is based on normalized question
- Slight variations in wording may not hit cache
- Cache expires after 1 hour by default
- Document updates invalidate related cache entries

### 4. Citations
- Always verify citations before making decisions
- Check page numbers and document versions
- Cross-reference with original documents when needed

### 5. Confidence Scores
- **0.8 - 1.0**: High confidence, likely accurate
- **0.5 - 0.8**: Medium confidence, verify if important
- **0.0 - 0.5**: Low confidence, may need clarification

---

## SDK Examples

### JavaScript/TypeScript
```typescript
async function askQuestion(question: string): Promise<AskResponse> {
  const response = await fetch('http://localhost:8080/api/v1/ask', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ question }),
  });
  
  return await response.json();
}

// Usage
const result = await askQuestion('What is the PTO policy?');
console.log(result.answer);
console.log('Citations:', result.citations);
```

### Python
```python
import requests

def ask_question(question: str) -> dict:
    response = requests.post(
        'http://localhost:8080/api/v1/ask',
        json={'question': question}
    )
    return response.json()

# Usage
result = ask_question('What is the PTO policy?')
print(result['answer'])
print('Confidence:', result['confidence'])
```

### Java
```java
RestTemplate restTemplate = new RestTemplate();
AskRequest request = new AskRequest();
request.setQuestion("What is the PTO policy?");

ResponseEntity<AskResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/v1/ask",
    request,
    AskResponse.class
);

AskResponse result = response.getBody();
System.out.println(result.getAnswer());
```

---

## Webhooks (Future Feature)

Future versions may support webhooks for:
- Document upload completion
- Cache invalidation events
- System alerts
- Usage threshold notifications

---

## Changelog

### Version 1.0.0 (November 2025)
- Initial release
- Question answering with citations
- Document upload (PDF, DOCX, Markdown, TXT)
- PII redaction
- Caching
- Clarification loop
- Model routing
- Metrics tracking

---

For more information, see the [README.md](README.md) and [QUICKSTART.md](QUICKSTART.md).

