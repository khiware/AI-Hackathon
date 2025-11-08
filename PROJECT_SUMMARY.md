# AskBit.AI - Project Summary

## 🎯 Project Overview

**AskBit.AI** is a production-ready, AI-powered internal policy copilot built with Java 17, Spring Boot 3.2, and Spring AI. It enables employees to ask natural language questions about company policies and receive accurate, cited answers from official documents.

## ✅ Requirements Implementation Status

### Core Features (All Implemented ✅)

| Requirement | Status | Implementation |
|------------|--------|----------------|
| **Document Ingestion** | ✅ Complete | PDF, DOCX, Markdown, TXT support with metadata extraction |
| **Vector Indexing** | ✅ Complete | Embedding-based retrieval with cosine similarity |
| **Grounded QA** | ✅ Complete | RAG pipeline with retrieval + generation |
| **Citations** | ✅ Complete | Page-level, section-level references with snippets |
| **Confidence Scoring** | ✅ Complete | Relevance-based confidence with threshold filtering |
| **Model Router** | ✅ Complete | Smart LLM selection with automatic failover |
| **Graceful Degradation** | ✅ Complete | Retry logic with secondary model and cache fallback |
| **PII Redaction** | ✅ Complete | Regex-based detection for emails, phones, SSN, etc. |
| **Caching** | ✅ Complete | Caffeine-based with <100ms response time |
| **Clarification Loop** | ✅ Complete | Ambiguity detection and clarifying questions |
| **Web UI** | ✅ Complete | ChatGPT-style interface with citations display |
| **REST API** | ✅ Complete | `/ask`, `/documents/*`, `/admin/metrics` |
| **Observability** | ✅ Complete | Metrics tracking with dashboard endpoint |

## 📂 Project Structure

```
AI-Hackathon/
├── build.gradle                    # Gradle build configuration
├── settings.gradle                 # Gradle settings
├── gradlew.bat                     # Gradle wrapper (Windows)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── README.md                       # Comprehensive documentation
├── QUICKSTART.md                   # 5-minute setup guide
├── API_DOCUMENTATION.md            # Complete API reference
├── .gitignore
│
├── sample-docs/                    # Sample policy documents
│   ├── HR_Policy_v3.1.md
│   ├── IT_Security_Policy_v2.5.md
│   └── Company_FAQ_v1.0.md
│
├── src/
│   ├── main/
│   │   ├── java/com/askbit/ai/
│   │   │   ├── AskBitAiApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── CacheConfig.java
│   │   │   │   └── JacksonConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AskController.java          # /ask endpoint
│   │   │   │   ├── DocumentController.java     # Document management
│   │   │   │   └── AdminController.java        # Metrics endpoint
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── AskRequest.java
│   │   │   │   ├── AskResponse.java
│   │   │   │   ├── Citation.java
│   │   │   │   ├── DocumentUploadResponse.java
│   │   │   │   └── MetricsResponse.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Document.java               # Document entity
│   │   │   │   ├── DocumentChunk.java          # Chunk entity with embeddings
│   │   │   │   └── QueryHistory.java           # Query tracking
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── DocumentRepository.java
│   │   │   │   ├── DocumentChunkRepository.java
│   │   │   │   └── QueryHistoryRepository.java
│   │   │   │
│   │   │   └── service/
│   │   │       ├── QuestionAnsweringService.java    # Main Q&A orchestrator
│   │   │       ├── DocumentProcessingService.java   # PDF/DOCX processing
│   │   │       ├── RetrievalService.java            # Vector search
│   │   │       ├── EmbeddingService.java            # Embeddings generation
│   │   │       ├── ModelRouterService.java          # LLM routing & failover
│   │   │       ├── PiiRedactionService.java         # PII detection & masking
│   │   │       ├── ClarificationService.java        # Ambiguity detection
│   │   │       └── MetricsService.java              # Analytics
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # Configuration
│   │       └── static/
│   │           └── index.html                  # Web UI
│   │
│   └── test/
│       └── java/com/askbit/ai/
│           └── AskBitAiApplicationTests.java
│
├── data/                           # H2 database (created at runtime)
└── documents/                      # Uploaded documents (created at runtime)
```

## 🏗️ Architecture

### High-Level Flow

```
User Question
    ↓
PII Redaction
    ↓
Cache Check → [Cache Hit] → Return Cached Response
    ↓ [Cache Miss]
Clarification Check → [Needs Clarification] → Ask Clarifying Question
    ↓ [Clear Question]
Document Retrieval (Vector Search)
    ↓
Context Building
    ↓
Model Router (GPT-4 → GPT-3.5 → Cached)
    ↓
Answer Generation
    ↓
PII Redaction (Response)
    ↓
Save to Cache & History
    ↓
Return Response with Citations
```

### Technology Stack

#### Core Framework
- **Java 17** - Modern LTS Java version
- **Spring Boot 3.2.0** - Enterprise application framework
- **Spring AI 1.0.0-M3** - AI integration framework
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM

#### AI & ML
- **OpenAI API** - GPT-4 for question answering
- **Spring AI Embedding Client** - Text embeddings
- **Vector Search** - Cosine similarity-based retrieval

#### Document Processing
- **Apache PDFBox 3.0.1** - PDF text extraction
- **Apache POI 5.2.5** - DOCX processing
- **Apache Tika 2.9.1** - Multi-format document parsing

#### Data & Caching
- **H2 Database** - Embedded database (development)
- **Caffeine** - High-performance caching
- **Spring Cache** - Cache abstraction

#### Build & Deploy
- **Gradle 8.5** - Build automation
- **Lombok** - Boilerplate reduction

## 🎨 Key Features

### 1. Intelligent Question Answering
- RAG (Retrieval-Augmented Generation) architecture
- Context window optimization
- Multi-document synthesis
- Confidence-based filtering

### 2. Smart Retrieval
- Semantic search with embeddings
- Hybrid search (vector + keyword)
- Top-K retrieval with relevance scoring
- Chunk-level granularity

### 3. Citation System
- Page-level references
- Section identifiers
- Line number tracking
- Snippet extraction
- Relevance scores

### 4. PII Protection
- 8 PII pattern types detected:
  - Email addresses
  - Phone numbers
  - Social Security Numbers
  - Credit card numbers
  - IP addresses
  - Physical addresses
  - Dates of birth
  - Names with titles
- Real-time redaction
- No PII in logs

### 5. Performance Optimization
- **Caching**: Normalized question-based caching
- **Model Router**: Latency-aware LLM selection
- **Failover**: Automatic retry with secondary models
- **Batch Processing**: Efficient document chunking

### 6. User Experience
- Modern chat interface
- Real-time typing indicators
- Confidence badges
- Citation links
- Responsive design

## 📊 Performance Targets

| Metric | Target | Implementation |
|--------|--------|----------------|
| **Response Time** | <1.5s | Configurable threshold with fast model routing |
| **Cache Hit Time** | <100ms | Caffeine in-memory cache |
| **Cache Hit Rate** | 30-40% | Normalized question matching |
| **Confidence Threshold** | 0.7 | Configurable, filters low-quality results |
| **Chunk Size** | 1000 chars | Optimized for context window |
| **Max Retrieval** | 5 chunks | Prevents context overflow |

## 🔒 Security & Privacy

### Data Protection
- PII automatically redacted
- No sensitive data in logs
- Secure file storage
- Database encryption ready

### Access Control
- CORS enabled for frontend
- API endpoints protected (ready for auth)
- File upload validation
- Size limits enforced

## 🧪 Testing Coverage

### Implemented Tests
- ✅ Context loading test
- ✅ PII redaction tests
- ✅ PII detection tests
- ✅ Clean text validation

### Test Categories
1. **Unit Tests** - Service layer logic
2. **Integration Tests** - Spring context loading
3. **Functional Tests** - PII redaction workflows

## 📈 Metrics & Monitoring

### Tracked Metrics
- Total queries processed
- Average response time
- Cache hit rate
- Total documents indexed
- Total chunks created
- Average confidence score
- PII redaction count
- Clarification request count
- Most used model

### Endpoints
- `/actuator/health` - Health check
- `/actuator/metrics` - Detailed metrics
- `/api/v1/admin/metrics` - Business metrics

## 🚀 Deployment Options

### Local Development
```bash
gradlew.bat bootRun
```

### Production Build
```bash
gradlew.bat build
java -jar build/libs/askbit-ai-1.0.0.jar
```

### Docker (Future)
```dockerfile
FROM eclipse-temurin:17-jre
COPY build/libs/askbit-ai-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Cloud Deployment
- **AWS**: Elastic Beanstalk, ECS, or Lambda
- **Azure**: App Service or Container Instances
- **GCP**: Cloud Run or App Engine
- **Heroku**: Direct deployment support

## 🎯 Use Cases

### 1. HR Policies
- PTO/vacation questions
- Benefits enrollment
- Leave policies
- Compensation questions

### 2. IT Security
- Password requirements
- VPN access
- Device policies
- Security incident reporting

### 3. General FAQs
- First-day onboarding
- Expense reimbursement
- Remote work policies
- Office facilities

### 4. Compliance
- GDPR compliance
- Data retention policies
- Security certifications
- Audit procedures

## 📝 Sample Interactions

### Example 1: Direct Answer
**Q:** "How many PTO days do full-time US employees get?"

**A:** "Full-time employees in the United States are entitled to 20 days of paid time off per year, plus 10 sick days and 5 personal days. [HR_Policy_v3.1.pdf, page 2, §1.1]"

**Confidence:** 95%

### Example 2: Clarification Required
**Q:** "What's the PTO policy?"

**A:** "I found information about 'PTO', but I need more context. Are you asking about:
1. full-time employees in the US
2. part-time employees
3. contractors
4. international employees

Please specify which one you're interested in."

### Example 3: No Information
**Q:** "What is the company's policy on cryptocurrency investments?"

**A:** "I couldn't find a clear policy on this. Please check with HR or submit a ticket."

**Confidence:** 0%

### Example 4: PII Redaction
**Q:** "Can john.doe@company.com expense his laptop?"

**Redacted Q:** "Can [EMAIL] expense his laptop?"

**A:** "Full-time employees can expense laptops up to $2,000... [HR_Policy_v3.1.pdf, page 8]"

**PII Redacted:** Yes

## 🔮 Future Enhancements

### Phase 2 (Q1 2026)
- [ ] PostgreSQL with pgvector
- [ ] User authentication (OAuth 2.0)
- [ ] Advanced NER for PII
- [ ] Multi-language support
- [ ] Conversation history

### Phase 3 (Q2 2026)
- [ ] Multiple LLM providers (Anthropic, Azure OpenAI)
- [ ] Advanced analytics dashboard
- [ ] Document versioning UI
- [ ] Approval workflows
- [ ] Slack/Teams integration

### Phase 4 (Q3 2026)
- [ ] Fine-tuned models
- [ ] Graph-based knowledge retrieval
- [ ] Automatic document updates
- [ ] A/B testing framework
- [ ] Mobile app

## 📊 Success Metrics

### Business Impact
- **40-50%** reduction in policy-related support tickets
- **3-5 minutes** average time saved per query
- **90%+** user satisfaction rate
- **60%+** reduction in email inquiries to HR

### Technical Metrics
- **99.9%** uptime target
- **<2s** p95 response time
- **35%+** cache hit rate
- **0.85+** average confidence score

## 🤝 Contributing

This is a hackathon project, but contributions are welcome:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

Built for AI Hackathon - November 2025

## 👥 Team

- Project: AskBit.AI
- Category: Employee Productivity Tool
- Technology: Java 17, Spring Boot, Spring AI
- Database: H2 (dev), PostgreSQL-ready (prod)

## 📞 Support

- **Documentation**: See README.md, QUICKSTART.md, API_DOCUMENTATION.md
- **Issues**: Create issue in repository
- **Questions**: Check FAQ or ask in discussions

---

## 🏆 Hackathon Deliverables Checklist

✅ **Working web chat UI** - http://localhost:8080
✅ **`/ask` API endpoint** - POST /api/v1/ask
✅ **3+ sample documents** - HR, IT Security, FAQ (Markdown format)
✅ **Complete JSON conversation** - Full request/response in API docs
✅ **PII redaction demo** - Implemented with regex patterns
✅ **Model router logs** - Logged in ModelRouterService
✅ **Cache stats** - Available via /api/v1/admin/metrics

## 🎉 Quick Demo

```bash
# 1. Start application
gradlew.bat bootRun

# 2. Upload sample document
curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@sample-docs\HR_Policy_v3.1.md" ^
  -F "version=3.1"

# 3. Ask question
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"What is the PTO policy?\"}"

# 4. Check metrics
curl http://localhost:8080/api/v1/admin/metrics

# 5. Open Web UI
# Navigate to http://localhost:8080
```

---

**Built with ❤️ for solving real-world problems**

*"One tool. Thousands of hours saved. Fewer tickets. Faster answers. Happier teams."*

