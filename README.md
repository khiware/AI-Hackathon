# AskBit.AI - AI-Powered Internal Policy Copilot

## 🎯 Overview

AskBit.AI is an intelligent internal policy copilot that answers employee questions about company policies, processes, and FAQs — accurately, securely, and fast — using only your organization's official documents.

## ✨ Features

### Core Capabilities
- ✅ **Grounded Question Answering** - Answers based only on company documents
- ✅ **Page-Level Citations** - Every answer includes source references
- ✅ **No Hallucinations** - Declines when information is not found
- ✅ **Multi-Format Support** - PDF, DOCX, Markdown, and text files
- ✅ **Document Versioning** - Track policy versions (e.g., HR_Policy_v3.1.pdf)
- ✅ **PII Detection & Redaction** - Automatic PII detection and masking
- ✅ **Smart Caching** - Fast responses for popular questions (<100ms)
- ✅ **Clarification Loop** - Asks for details when questions are ambiguous
- ✅ **Model Router** - Smart LLM selection based on latency and cost
- ✅ **Graceful Degradation** - Automatic failover when primary model fails
- ✅ **Observability** - Track metrics, latency, cache hit rate, and more

## 🏗️ Architecture

```
┌─────────────────┐
│   Web UI/API    │
└────────┬────────┘
         │
┌────────▼────────────────────────────────────────┐
│              AskBit.AI Core                     │
│  ┌──────────────┐  ┌───────────────────────┐   │
│  │ Question     │  │ Document Processing   │   │
│  │ Answering    │  │ Service               │   │
│  └──────┬───────┘  └───────────┬───────────┘   │
│         │                       │               │
│  ┌──────▼───────┐  ┌───────────▼───────────┐   │
│  │ Retrieval    │  │ Embedding Service     │   │
│  │ Service      │  │ (Vector Search)       │   │
│  └──────┬───────┘  └───────────────────────┘   │
│         │                                       │
│  ┌──────▼───────────────────────────────────┐  │
│  │         Model Router                     │  │
│  │  (OpenAI GPT-4 → GPT-3.5 → Cache)       │  │
│  └──────────────────────────────────────────┘  │
│                                                 │
│  ┌──────────────┐  ┌───────────────────────┐   │
│  │ PII          │  │ Clarification         │   │
│  │ Redaction    │  │ Service               │   │
│  └──────────────┘  └───────────────────────┘   │
└─────────────────────────────────────────────────┘
         │
┌────────▼────────┐
│   H2 Database   │
│  (Documents,    │
│   Chunks, Cache)│
└─────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 8.x
- OpenAI API Key (or compatible LLM endpoint)

### Installation

1. **Clone the repository**
```bash
cd "C:\Users\kiranh\Desktop\AI Hackathon\AI-Hackathon"
```

2. **Set up environment variables**
```bash
set OPENAI_API_KEY=your-api-key-here
```

Or edit `src/main/resources/application.properties`:
```properties
spring.ai.openai.api-key=your-api-key-here
```

3. **Build the project**
```bash
gradlew.bat build
```

4. **Run the application**
```bash
gradlew.bat bootRun
```

The application will start on `http://localhost:8080`

## 📖 Usage

### Web UI
1. Open your browser and navigate to `http://localhost:8080`
2. Type your question in the chat interface
3. Get instant answers with citations!

### REST API

#### Ask a Question
```bash
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"Can I expense a laptop if I'm a contractor in Germany?\"}"
```

**Response:**
```json
{
  "answer": "Yes, contractors in Germany can expense laptops up to €800.",
  "citations": [
    {
      "documentId": "abc123",
      "fileName": "HR_Policy_v3.1.pdf",
      "version": "3.1",
      "pageNumber": 12,
      "section": "§4.2",
      "snippet": "Contractors in Germany are eligible to expense equipment...",
      "relevanceScore": 0.92
    }
  ],
  "confidence": 0.92,
  "cached": false,
  "responseTimeMs": 1234,
  "modelUsed": "openai-gpt4",
  "piiRedacted": false
}
```

#### Upload a Document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@HR_Policy.pdf" ^
  -F "version=3.1" ^
  -F "description=Human Resources Policy Document"
```

#### Get Metrics
```bash
curl http://localhost:8080/api/v1/admin/metrics
```

**Response:**
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

#### Get All Documents
```bash
curl http://localhost:8080/api/v1/documents
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/askbit/ai/
│   │   ├── AskBitAiApplication.java
│   │   ├── config/
│   │   │   ├── CacheConfig.java
│   │   │   └── JacksonConfig.java
│   │   ├── controller/
│   │   │   ├── AskController.java
│   │   │   ├── DocumentController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   │   ├── AskRequest.java
│   │   │   ├── AskResponse.java
│   │   │   ├── Citation.java
│   │   │   ├── DocumentUploadResponse.java
│   │   │   └── MetricsResponse.java
│   │   ├── model/
│   │   │   ├── Document.java
│   │   │   ├── DocumentChunk.java
│   │   │   └── QueryHistory.java
│   │   ├── repository/
│   │   │   ├── DocumentRepository.java
│   │   │   ├── DocumentChunkRepository.java
│   │   │   └── QueryHistoryRepository.java
│   │   └── service/
│   │       ├── QuestionAnsweringService.java
│   │       ├── DocumentProcessingService.java
│   │       ├── RetrievalService.java
│   │       ├── EmbeddingService.java
│   │       ├── ModelRouterService.java
│   │       ├── PiiRedactionService.java
│   │       ├── ClarificationService.java
│   │       └── MetricsService.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── index.html
└── test/
    └── java/
```

## 🔧 Configuration

Key configurations in `application.properties`:

```properties
# Confidence threshold for answers (0.0 - 1.0)
askbit.ai.confidence-threshold=0.7

# Maximum number of document chunks to retrieve
askbit.ai.max-retrieval-results=5

# Cache TTL in seconds
askbit.ai.cache.ttl-seconds=3600

# Enable/disable PII redaction
askbit.ai.pii-redaction.enabled=true

# Document storage path
askbit.ai.document.storage-path=./documents

# Model router latency threshold (ms)
askbit.ai.model.router.latency-threshold-ms=1500

# Enable fallback mechanism
askbit.ai.model.router.fallback-enabled=true
```

## 📊 Features Implemented

### ✅ Completed
1. **Document Ingestion & Indexing** - PDF, DOCX, Markdown, text files
2. **Grounded Question Answering** - RAG-based with citations
3. **Citation & Confidence Control** - Page-level references
4. **Model Router** - Smart LLM selection
5. **Graceful Degradation** - Automatic failover
6. **PII Detection & Redaction** - Regex + pattern-based
7. **Warm Cache** - Caffeine-based caching
8. **Clarification Loop** - Detects ambiguous questions
9. **Web UI** - ChatGPT-like interface
10. **REST API** - Complete endpoints
11. **Observability** - Metrics and monitoring

## 🎯 Sample Questions to Try

1. "What is our PTO policy for full-time employees?"
2. "Can I work remotely from another country?"
3. "What expenses can I claim for business travel?"
4. "What are the health insurance benefits?"
5. "How do I request parental leave?"

## 🛠️ Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring AI 1.0.0-M3** - AI integration
- **Spring Data JPA** - Database access
- **H2 Database** - Embedded database (development)
- **Apache PDFBox** - PDF processing
- **Apache POI** - DOCX processing
- **Caffeine** - Caching
- **Lombok** - Code generation
- **Gradle** - Build tool

## 📈 Performance Metrics

- **P95 Latency**: Target <1.5s (configurable)
- **Cache Hit Rate**: ~30-40% for common queries
- **Response Time**: <100ms for cached queries
- **Accuracy**: Confidence-based with citations

## 🔒 Security & Privacy

- **PII Redaction**: Automatic detection and masking of sensitive information
- **No External Data**: Uses only provided documents
- **Local Processing**: Can run offline with local LLMs (optional)
- **Secure Logging**: PII is never logged

## 🚧 Future Enhancements

- PostgreSQL with pgvector for production
- Multiple LLM provider support (Anthropic, Azure OpenAI, etc.)
- Advanced NER for PII detection
- Multi-language support
- User authentication and authorization
- Advanced analytics dashboard
- Document update notifications
- Conversation history

## 📝 License

This project is created for the AI Hackathon.

## 🤝 Contributing

This is a hackathon project. Feel free to fork and enhance!

## 📧 Support

For questions or issues, please create an issue in the repository.

---

**Built with ❤️ for the AI Hackathon**

*One tool. Thousands of hours saved. Fewer tickets. Faster answers. Happier teams.*

