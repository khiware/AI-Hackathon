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
- ✅ **Hybrid Search** - Combines vector similarity and keyword matching
- ✅ **Model Router** - Smart LLM selection with automatic failover
- ✅ **Graceful Degradation** - Continues working even when primary model fails
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
│  │ Hybrid       │  │ Embedding Service     │   │
│  │ Retrieval    │  │ (Vector + Keyword)    │   │
│  └──────┬───────┘  └───────────────────────┘   │
│         │                                       │
│  ┌──────▼───────────────────────────────────┐  │
│  │         Model Router                     │  │
│  │  (GPT-4 → GPT-3.5 → Cache → Fallback)   │  │
│  └──────────────────────────────────────────┘  │
│                                                 │
│  ┌──────────────┐  ┌───────────────────────┐   │
│  │ PII          │  │ Query Preprocessing   │   │
│  │ Redaction    │  │ & Clarification       │   │
│  └──────────────┘  └───────────────────────┘   │
└─────────────────────────────────────────────────┘
         │
┌────────▼────────┐
│  PostgreSQL DB  │
│  + Redis Cache  │
│  (Documents,    │
│   Chunks, Vector│
│   Embeddings)   │
└─────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **Gradle 8.x**
- **PostgreSQL** (for production) or H2 (for development/testing)
- **Redis** (for caching)
- **OpenAI API Key**

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd AI-Hackathon
```

2. **Set up environment variables**

For Windows:
```cmd
set OPENAI_API_KEY=your-api-key-here
```

For Linux/Mac:
```bash
export OPENAI_API_KEY=your-api-key-here
```

Or edit `src/main/resources/application.properties`:
```properties
spring.ai.openai.api-key=your-api-key-here
spring.ai.openai.chat.options.model=gpt-4
```

3. **Configure Database (Optional)**

By default, the application uses H2 in-memory database. For production, configure PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/askbitdb
spring.datasource.username=your-username
spring.datasource.password=your-password
```

4. **Configure Redis (Optional)**

Configure Redis for distributed caching:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

5. **Build the project**
```bash
./gradlew build
```

Or on Windows:
```cmd
gradlew.bat build
```

6. **Run the application**
```bash
./gradlew bootRun
```

Or on Windows:
```cmd
gradlew.bat bootRun
```

The application will start on `http://localhost:8080`

## 📖 Usage

### Web UI
1. Open your browser and navigate to `http://localhost:8080`
2. Upload policy documents via the admin interface at `http://localhost:8080/admin.html`
3. Ask questions through the main interface
4. Get instant answers with citations!

### REST API Examples

#### 1. Ask a Question
```bash
curl -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the leave policy?"}'
```

**Response:**
```json
{
  "answer": "Employees are entitled to 15 days of paid leave per year...",
  "citations": [
    {
      "documentId": "abc123",
      "fileName": "HR_Policy_v3.1.pdf",
      "version": "3.1",
      "pageNumber": 12,
      "section": "Leave Policy",
      "relevanceScore": 0.92
    }
  ],
  "confidence": 0.92,
  "cached": false,
  "needsClarification": false,
  "responseTimeMs": 1234,
  "modelUsed": "gpt-4",
  "piiRedacted": false
}
```

#### 2. Ask a Question with Context (Clarification)
```bash
curl -X POST http://localhost:8080/api/v1/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the expense policy?",
    "context": "travel expenses for international trips"
  }'
```

#### 3. Upload a Document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@HR_Policy.pdf" \
  -F "version=3.1" \
  -F "description=Human Resources Policy Document"
```

**Response:**
```json
{
  "documentId": "doc_abc123",
  "fileName": "HR_Policy.pdf",
  "version": "3.1",
  "success": true,
  "message": "Document uploaded and processed successfully",
  "pagesProcessed": 45,
  "chunksCreated": 150
}
```

#### 4. Get All Documents
```bash
curl http://localhost:8080/api/v1/documents
```

#### 5. Delete a Document
```bash
curl -X DELETE http://localhost:8080/api/v1/documents/{documentId}
```

#### 6. Get System Metrics
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
  "mostUsedModel": "gpt-4"
}
```

#### 7. Get Top Questions
```bash
curl http://localhost:8080/api/v1/admin/top-questions?limit=10
```

#### 8. Get Cache Statistics
```bash
curl http://localhost:8080/api/v1/admin/cache/stats
```

#### 9. Invalidate Cache
```bash
curl -X POST http://localhost:8080/api/v1/admin/cache/invalidate
```

#### 10. Hybrid Search (Advanced)
```bash
curl http://localhost:8080/api/search/hybrid?query=leave%20policy&topK=5
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/askbit/ai/
│   │   ├── AskBitAiApplication.java          # Main Spring Boot application
│   │   ├── config/
│   │   │   ├── CacheConfig.java              # Redis cache configuration
│   │   │   └── JacksonConfig.java            # JSON serialization config
│   │   ├── controller/
│   │   │   ├── AskController.java            # Question answering endpoint
│   │   │   ├── DocumentController.java       # Document upload/management
│   │   │   ├── AdminController.java          # Admin & metrics endpoints
│   │   │   └── HybridSearchController.java   # Advanced search endpoints
│   │   ├── dto/
│   │   │   ├── AskRequest.java               # Question request DTO
│   │   │   ├── AskResponse.java              # Answer response DTO
│   │   │   ├── Citation.java                 # Citation/source reference
│   │   │   ├── DocumentUploadResponse.java   # Upload response DTO
│   │   │   ├── MetricsResponse.java          # Metrics DTO
│   │   │   ├── TopQuestionResponse.java      # Top questions DTO
│   │   │   └── CacheStatsResponse.java       # Cache stats DTO
│   │   ├── model/
│   │   │   ├── Document.java                 # Document entity
│   │   │   ├── DocumentChunk.java            # Chunk entity with embeddings
│   │   │   └── QueryHistory.java             # Query history entity
│   │   ├── repository/
│   │   │   ├── DocumentRepository.java       # Document data access
│   │   │   ├── DocumentChunkRepository.java  # Chunk data access
│   │   │   └── QueryHistoryRepository.java   # Query history data access
│   │   └── service/
│   │       ├── QuestionAnsweringService.java # Main QA orchestration
│   │       ├── DocumentProcessingService.java# Document parsing & chunking
│   │       ├── RetrievalService.java         # Vector similarity search
│   │       ├── HybridRetrievalService.java   # Hybrid search (vector + keyword)
│   │       ├── EmbeddingService.java         # Generate embeddings
│   │       ├── ModelRouterService.java       # LLM routing & failover
│   │       ├── PiiRedactionService.java      # PII detection & redaction
│   │       ├── QueryPreprocessingService.java# Query normalization
│   │       ├── ClarificationService.java     # Clarification logic
│   │       ├── TemporalQueryAnalyzer.java    # Version/year analysis
│   │       ├── CacheService.java             # Redis cache operations
│   │       ├── MetricsService.java           # Metrics collection
│   │       └── AdminService.java             # Admin operations
│   └── resources/
│       ├── application.properties            # Application configuration
│       ├── static/
│       │   ├── index.html                    # Main UI
│       │   └── admin.html                    # Admin UI
│       └── initial-policy-documents/         # Sample documents
└── test/
    └── java/com/askbit/ai/
        └── AskBitAiApplicationTests.java     # Integration tests
```

## ⚙️ Configuration

### Key Configuration Properties

Edit `src/main/resources/application.properties`:

```properties
# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.embedding.options.model=text-embedding-ada-002

# Model Router Configuration
askbit.ai.models.primary=gpt-4
askbit.ai.models.secondary=gpt-3.5-turbo
askbit.ai.model.timeout-ms=30000
askbit.ai.model.router.fallback-enabled=true

# Search Configuration
askbit.ai.use-hybrid-search=true
askbit.ai.max-retrieval-results=5
askbit.ai.confidence-threshold=0.7

# PII Redaction
askbit.ai.pii-redaction.enabled=true

# Caching
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/askbitdb
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# Document Storage
askbit.ai.document.storage-path=./documents
```

## 🔒 Security Features

### PII Protection
AskBit.AI automatically detects and redacts the following PII:
- Email addresses
- Phone numbers
- Social Security Numbers (SSN)
- Credit card numbers
- IP addresses
- Physical addresses
- Dates of birth
- Personal names (when prefixed with titles)

**Note:** PII is never logged to application logs to ensure data privacy.

### Data Privacy
- All user queries are stored without PII
- Original questions are preprocessed and normalized before storage
- Logging is configured to exclude any user-identifiable information

## 🎯 Advanced Features

### 1. Hybrid Search
Combines vector similarity search (semantic understanding) with keyword matching for better accuracy:
- **Vector Search (70%)**: Understands meaning and context
- **Keyword Search (30%)**: Exact term matching

### 2. Temporal Query Analysis
Automatically handles version-specific queries:
- "What was the leave policy in 2023?"
- "Show me the current expense policy"
- "What changed in the latest version?"

### 3. Clarification Loop
Asks for clarification when questions are ambiguous:
- Detects vague terms (e.g., "leave" → vacation vs. sick leave)
- Handles incomplete queries
- Iterative refinement support

### 4. Model Failover
Graceful degradation with multiple fallback levels:
1. Primary model (GPT-4)
2. Secondary model (GPT-3.5)
3. Cached response (if available)
4. User-friendly error message

### 5. Query Preprocessing
Automatically fixes common issues:
- Spelling corrections
- Abbreviation expansion
- Text speak normalization
- Special character handling

## 📊 Monitoring & Observability

### Metrics Endpoint
Access real-time metrics at `/api/v1/admin/metrics`:
- Total queries processed
- Average response time
- Cache hit rate
- Model usage distribution
- PII redaction count
- Clarification requests

### Cache Statistics
Monitor cache performance at `/api/v1/admin/cache/stats`:
- Cache size
- Hit/miss rate
- Most cached queries

### Top Questions
Identify common queries at `/api/v1/admin/top-questions`:
- Question frequency
- Average confidence
- Last asked timestamp

## 🧪 Testing

Run all tests:
```bash
./gradlew test
```

Run with coverage:
```bash
./gradlew test jacocoTestReport
```

## 🚢 Deployment

### Docker Deployment (Recommended)

1. Create a `Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

2. Build and run:
```bash
docker build -t askbit-ai .
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/askbitdb \
  askbit-ai
```

### Cloud Deployment

#### AWS
- Deploy on **AWS Elastic Beanstalk** or **ECS**
- Use **RDS PostgreSQL** for database
- Use **ElastiCache Redis** for caching
- Store documents in **S3**

#### Azure
- Deploy on **Azure App Service**
- Use **Azure Database for PostgreSQL**
- Use **Azure Cache for Redis**
- Store documents in **Azure Blob Storage**

#### GCP
- Deploy on **Google Cloud Run** or **GKE**
- Use **Cloud SQL PostgreSQL**
- Use **Memorystore for Redis**
- Store documents in **Cloud Storage**

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Built with **Spring AI** framework
- Powered by **OpenAI GPT-4**
- Vector search with **pgvector** extension
- Caching with **Redis**

## 📧 Support

For questions, issues, or feature requests:
- Open an issue on GitHub
- Contact the development team

## 🔄 Version History

### v1.0.0 (Current)
- Initial release
- Grounded question answering
- Multi-format document support
- PII detection and redaction
- Hybrid search
- Model failover
- Admin dashboard
- Metrics and observability

---

**Made with ❤️ for better employee experiences**

