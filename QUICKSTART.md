# Quick Start Guide - AskBit.AI

## 🚀 Get Running in 5 Minutes

### Step 1: Set Your OpenAI API Key

Option A - Environment Variable (Recommended):
```bash
set OPENAI_API_KEY=sk-your-api-key-here
```

Option B - Edit Configuration File:
Edit `src\main\resources\application.properties` and set:
```properties
spring.ai.openai.api-key=sk-your-api-key-here
```

### Step 2: Build the Project

```bash
gradlew.bat build
```

### Step 3: Run the Application

```bash
gradlew.bat bootRun
```

Wait for the message: `Started AskBitAiApplication`

### Step 4: Open Your Browser

Navigate to: http://localhost:8080

You should see the AskBit.AI chat interface!

### Step 5: Upload Sample Documents

Upload the sample policy documents using the API:

```bash
curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@sample-docs\HR_Policy_v3.1.md" ^
  -F "version=3.1" ^
  -F "description=HR Policy Document"

curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@sample-docs\IT_Security_Policy_v2.5.md" ^
  -F "version=2.5" ^
  -F "description=IT Security Policy"

curl -X POST http://localhost:8080/api/v1/documents/upload ^
  -F "file=@sample-docs\Company_FAQ_v1.0.md" ^
  -F "version=1.0" ^
  -F "description=Company FAQ"
```

Or use Postman/Insomnia to upload via the UI.

### Step 6: Ask Questions!

Try these sample questions in the web UI:

1. "How many PTO days do full-time US employees get?"
2. "Can contractors expense laptops?"
3. "What is the password policy?"
4. "Can I work remotely from another country?"
5. "What health insurance benefits are available?"

## 🔍 Verify Installation

### Check Health Endpoint
```bash
curl http://localhost:8080/api/v1/health
```

Expected response: `AskBit.AI is running`

### Check Metrics
```bash
curl http://localhost:8080/api/v1/admin/metrics
```

### Check Documents
```bash
curl http://localhost:8080/api/v1/documents
```

## 📝 Sample API Call

Test the `/ask` endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"question\": \"What is the PTO policy for full-time employees?\"}"
```

## 🐛 Troubleshooting

### Issue: "OpenAI API key not configured"
**Solution**: Make sure you set the OPENAI_API_KEY environment variable or update application.properties

### Issue: "Port 8080 already in use"
**Solution**: Change the port in application.properties:
```properties
server.port=8081
```

### Issue: "Build failed"
**Solution**: Make sure you have Java 17 installed:
```bash
java -version
```

### Issue: "Documents not uploading"
**Solution**: Check that the `documents` folder exists and has write permissions

### Issue: "Embeddings not working"
**Solution**: Verify your OpenAI API key has access to the embeddings API

## 📚 Next Steps

1. **Explore the Web UI** - Chat interface at http://localhost:8080
2. **Upload your own documents** - PDF, DOCX, or Markdown files
3. **Check the metrics dashboard** - See cache hit rates and performance
4. **Customize settings** - Edit application.properties for your needs
5. **Read the full README** - Comprehensive documentation

## 🎯 Key Features to Try

### 1. Citations
Ask: "What equipment can I expense?"
Notice the answer includes source references!

### 2. Confidence Scores
Each answer shows how confident the AI is (0-100%)

### 3. Cached Responses
Ask the same question twice - second time is instant! ⚡

### 4. PII Redaction
Try asking with an email or phone number - it will be automatically redacted

### 5. Clarification
Ask a vague question like "What's the policy?" - the AI will ask for clarification

### 6. No Hallucination
Ask about something not in the documents - the AI will say it doesn't know

## 💡 Pro Tips

1. **Upload documents first** - The system needs documents to answer questions
2. **Be specific** - More specific questions get better answers
3. **Check citations** - Always verify the source of information
4. **Use cache** - Common questions are served from cache (<100ms)
5. **Monitor metrics** - Track performance and usage patterns

## 🔗 Important URLs

- **Web UI**: http://localhost:8080
- **API Docs**: See README.md for full API documentation
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:file:./data/askbitdb)
- **Actuator Health**: http://localhost:8080/actuator/health

## 🛟 Need Help?

- Check the README.md for detailed documentation
- Look at sample documents in `sample-docs/` folder
- Review the code in `src/main/java/com/askbit/ai/`

## 🎉 You're All Set!

Start asking questions and let AskBit.AI help your team find policy information faster!

---

**Built for the AI Hackathon** 🏆
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists

