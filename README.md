# AskBit.AI - AI-Powered Internal Policy Copilot

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technical Approach](#-technical-approach)
- [Architecture](#️-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Step-by-Step Execution Guide](#-step-by-step-execution-guide)
- [Troubleshooting](#-troubleshooting)
- [Usage](#-usage)
  - [Web UI](#web-ui)
  - [REST API Examples](#rest-api-examples)
- [Project Structure](#-project-structure)
- [Dependencies](#-dependencies)
- [Configuration](#️-configuration)
- [Security Features](#-security-features)
- [Advanced Features](#-advanced-features)
- [Monitoring & Observability](#-monitoring--observability)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)
- [Support](#-support)
- [Version History](#-version-history)

## 🎯 Overview

### Build AskBit.AI - An Intelligent Internal Policy Copilot

AskBit.AI is an intelligent internal policy copilot that answers employee questions about company policies, processes, and FAQs — **accurately, securely, and fast** — using only your organization's official documents.

### The Problem

Organizations face significant challenges in making internal policies accessible to employees:
- **Information Overload**: Employees struggle to find relevant information across hundreds of policy documents
- **Time Wastage**: HR teams spend countless hours answering repetitive questions
- **Outdated Information**: Employees often reference old policy versions, leading to compliance issues
- **Inconsistent Answers**: Different team members provide conflicting information
- **Security Concerns**: Sensitive company information needs protection

### Our Solution

The system delivers:

✅ **Grounded Answers with Citations** - All answers are based on real documents with page-level citations

✅ **No Hallucination** - Never fabricates information; declines or asks for clarification when unsure

✅ **Cost-Efficient & Resilient** - Intelligent caching and multi-LLM failover for optimal cost and reliability

✅ **PII Protection** - Automatic detection and redaction of sensitive information in prompts and responses

✅ **Performance Optimized** - Caches popular answers and asks clarifying questions when needed

### Business Impact

- 🎯 **Reduce policy-related support tickets** by providing instant self-service answers
- ⚡ **Cut query resolution time by 40–50%** compared to traditional methods
- 📈 **Improve employee productivity** by eliminating time spent searching for policies
- 🔒 **Ensure compliance** with accurate, version-tracked policy information

## ⚡ Quick Start

Get AskBit.AI running in 5 minutes:

```bash
# 1. Set up database (PostgreSQL with pgvector)
createdb askbitdb
psql -d askbitdb -c "CREATE EXTENSION vector;"

# 2. Start Redis
redis-server

# 3. Set your OpenAI API key
export OPENAI_API_KEY=sk-your-key-here  # Linux/Mac
set OPENAI_API_KEY=sk-your-key-here     # Windows

# 4. Update database credentials in src/main/resources/application.properties

# 5. Build and run
./gradlew bootRun    # Linux/Mac
gradlew.bat bootRun  # Windows

# 6. Access the application
# Open http://localhost:8080 in your browser
```

**First Steps:**
1. Upload a document at http://localhost:8080/admin.html
2. Ask questions at http://localhost:8080/index.html
3. Get instant, grounded answers with citations!

**Quick Testing:**
Run the automated end-to-end test script to verify everything is working:
```bash
test-askbitai.bat    # Windows
./test-askbitai.sh   # Linux/macOS
```
See the [Testing](#-testing) section for detailed information.

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

## 🧠 Technical Approach

### Overview
AskBit.AI implements a **Retrieval-Augmented Generation (RAG)** system with advanced features for enterprise deployment. The system is designed to provide accurate, grounded answers to employee questions using only company-approved documents.

### Key Design Decisions

#### 1. **Document Processing Pipeline**
- **Chunking Strategy**: Documents are split into semantic chunks (500-1000 tokens) with 100-token overlap to preserve context
- **Multi-format Support**: Uses Apache Tika for universal document parsing (PDF, DOCX, TXT, MD)
- **Vector Embeddings**: Each chunk is converted to a 1536-dimensional vector using OpenAI's `text-embedding-3-small` model
- **Storage**: Vectors stored in PostgreSQL with pgvector extension for efficient similarity search

#### 2. **Hybrid Retrieval System**
Combines two search strategies for optimal results:
- **Vector Similarity Search (70% weight)**: Semantic understanding using cosine similarity
- **Keyword/BM25 Search (30% weight)**: Exact term matching for technical terms and acronyms
- **Fusion Algorithm**: RRF (Reciprocal Rank Fusion) combines results from both methods

#### 3. **Intelligent Model Routing**
Multi-tier failover system for reliability:
```
GPT-4 Turbo (Primary)
    ↓ (on timeout/error)
GPT-3.5 Turbo (Secondary)
    ↓ (on failure)
Redis Cache (Cached responses)
    ↓ (on cache miss)
Graceful Error Message
```

#### 4. **Query Processing Pipeline**
1. **Preprocessing**: Normalize text, fix spelling, expand abbreviations
2. **PII Detection**: Identify and redact sensitive information
3. **Temporal Analysis**: Extract version/date requirements
4. **Clarification Check**: Determine if query is ambiguous
5. **Retrieval**: Fetch relevant chunks using hybrid search
6. **Generation**: Create grounded answer with citations
7. **Validation**: Ensure answer is supported by retrieved context

#### 5. **Caching Strategy**
- **L1 Cache**: Spring Cache with Redis backend (2-hour TTL)
- **Cache Key**: Hash of (question + context + version)
- **Invalidation**: Automatic on document updates, manual via admin API
- **Stats Tracking**: Hit rate, size, most cached queries

#### 6. **Security & Privacy**
- **PII Redaction**: Regex-based detection of emails, SSNs, phone numbers, etc.
- **Query Sanitization**: Remove malicious input before processing
- **No PII Logging**: Sensitive data never written to logs
- **Document Access Control**: Only indexed documents are searchable

### Technology Stack

**Backend Framework:**
- Spring Boot 3.2.0 (Java 17)
- Spring AI 1.0.0-M3 for LLM integration
- Spring Data JPA for database operations

**AI/ML Components:**
- OpenAI GPT-4 Turbo (primary model)
- OpenAI text-embedding-3-small (embeddings)
- Custom hybrid search implementation

**Database:**
- PostgreSQL 12+ with pgvector extension
- Vector similarity search using cosine distance

**Caching:**
- Redis 6+ with Lettuce client
- Spring Cache abstraction

**Document Processing:**
- Apache PDFBox 3.0.1 (PDF parsing)
- Apache POI 5.2.5 (Office documents)
- Apache Tika 2.9.1 (universal parser)

**Build & Deployment:**
- Gradle 8.x
- Spring Boot Actuator for monitoring
- Docker support

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

### Environment Variables

The following environment variables can be used to configure the application:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `OPENAI_API_KEY` | OpenAI API key for GPT-4 and embeddings | None | **Yes** |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/askbitdb` | No |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` | No |
| `SPRING_DATASOURCE_PASSWORD` | Database password | None | **Yes** |
| `SPRING_REDIS_HOST` | Redis server hostname | `127.0.0.1` | No |
| `SPRING_REDIS_PORT` | Redis server port | `6379` | No |
| `SPRING_REDIS_PASSWORD` | Redis password (if required) | Empty | No |
| `SERVER_PORT` | Application HTTP port | `8080` | No |

**Setting environment variables:**

Windows (Command Prompt):
```cmd
set OPENAI_API_KEY=sk-your-key
set SPRING_DATASOURCE_PASSWORD=your-db-password
```

Windows (PowerShell):
```powershell
$env:OPENAI_API_KEY="sk-your-key"
$env:SPRING_DATASOURCE_PASSWORD="your-db-password"
```

Linux/Mac:
```bash
export OPENAI_API_KEY=sk-your-key
export SPRING_DATASOURCE_PASSWORD=your-db-password
```

**Using .env file (recommended for development):**

Create a `.env` file in the project root:
```properties
OPENAI_API_KEY=sk-your-actual-api-key
SPRING_DATASOURCE_PASSWORD=your-database-password
SPRING_REDIS_PASSWORD=your-redis-password
```

**Note:** Never commit `.env` files or `application.properties` with secrets to version control!

### Configuration Profiles

The application supports Spring Boot profiles for different environments:

**Development (default):**
```properties
# src/main/resources/application.properties
spring.profiles.active=dev
```

**Production:**
```properties
# src/main/resources/application-prod.properties
spring.jpa.show-sql=false
logging.level.com.askbit=INFO
askbit.ai.cache.ttl-seconds=7200
```

Activate profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
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

### Unit & Integration Tests

Run all tests:
```bash
./gradlew test
```

Run with coverage:
```bash
./gradlew test jacocoTestReport
```

### End-to-End Testing

The project includes automated end-to-end test scripts that verify the complete system functionality, including document upload, question answering, caching, and admin endpoints.

#### Test Scripts Available

- **`test-askbitai.bat`** - Windows batch script
- **`test-askbitai.sh`** - Linux/macOS bash script

#### What the Scripts Test

1. ✅ Health endpoint verification
2. ✅ Document upload functionality
3. ✅ Document listing
4. ✅ Policy-specific questions:
   - "What should I do if I'm feeling unwell but scheduled to work in the office?"
   - "When will Variable Pay be typically paid?"
   - "How can employees maximize their Progressive Variable Pay?"
   - "How to track my compliance?"
5. ✅ Cache performance (re-asking same question)
6. ✅ System metrics retrieval
7. ✅ Top questions analytics
8. ✅ Cache statistics

#### How to Run

**Prerequisites:**
- Application running on `http://localhost:8080`
- Sample document available at `sample-docs/test-policy.pdf`
- `curl` installed on your system

**Windows:**
```cmd
test-askbitai.bat
```

**Linux/macOS:**
```bash
# Make the script executable (first time only)
chmod +x test-askbitai.sh

# Run the script
./test-askbitai.sh
```

**Note:** For better JSON formatting on Windows, you can install `jq` and modify the script to use `| jq` instead of removing the `json_pp` calls.

#### Expected Output

The script will:
1. Display formatted test results for each endpoint
2. Show response times and data
3. Verify caching is working (second identical query should be faster)
4. Display system metrics and statistics

#### Customizing Tests

To test with your own questions, edit the script files and modify the curl commands in the "Testing Specific Policy Questions" section.

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

- Built with **Spring Boot 3.2.0** and **Spring AI 1.0.0-M3**
- Powered by **OpenAI GPT-4 Turbo**
- Vector search with **pgvector** extension for PostgreSQL
- Caching with **Redis** (using Lettuce client)
- Document processing with **Apache PDFBox**, **Apache POI**, and **Apache Tika**

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

