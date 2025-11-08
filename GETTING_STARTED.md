# 🚀 Getting Started with AskBit.AI

Welcome! This guide will get you up and running with AskBit.AI in just a few minutes.

## 📋 What You'll Need

- **Java 17 or higher** installed
- **OpenAI API Key** (get one from https://platform.openai.com/api-keys)
- **Command prompt** or terminal access
- **Web browser** for the UI

## ⚡ Quick Start (5 Minutes)

### Option 1: Automated Setup (Recommended)

1. **Verify your setup**
   ```bash
   verify-setup.bat
   ```
   This checks if Java is installed and project files are in place.

2. **Set your OpenAI API Key**
   ```bash
   set OPENAI_API_KEY=sk-your-api-key-here
   ```

3. **Start the application**
   ```bash
   start.bat
   ```
   Wait for "Started AskBitAiApplication" message.

4. **Upload sample documents and test**
   Open a new terminal and run:
   ```bash
   demo.bat
   ```

5. **Open the web interface**
   Navigate to: http://localhost:8080

### Option 2: Manual Setup

1. **Set your API key**
   ```bash
   set OPENAI_API_KEY=sk-your-api-key-here
   ```

2. **Build the project**
   ```bash
   gradlew.bat build
   ```

3. **Run the application**
   ```bash
   gradlew.bat bootRun
   ```

4. **Upload a document** (in another terminal)
   ```bash
   curl -X POST http://localhost:8080/api/v1/documents/upload ^
     -F "file=@sample-docs\HR_Policy_v3.1.md" ^
     -F "version=3.1"
   ```

5. **Ask a question**
   ```bash
   curl -X POST http://localhost:8080/api/v1/ask ^
     -H "Content-Type: application/json" ^
     -d "{\"question\": \"What is the PTO policy?\"}"
   ```

## 🎯 Try These Sample Questions

Once you've uploaded the sample documents, try asking:

1. **HR Policy Questions:**
   - "How many PTO days do full-time US employees get?"
   - "Can contractors expense laptops?"
   - "What is the parental leave policy?"
   - "Can I work remotely from another country?"

2. **IT Security Questions:**
   - "What are the password requirements?"
   - "Do I need to use MFA?"
   - "What should I do if I lose my laptop?"
   - "What's the VPN policy?"

3. **General FAQ:**
   - "What should I do on my first day?"
   - "How do I submit expenses?"
   - "What training budget do I have?"
   - "Is there free parking?"

## 📁 Files Overview

| File | Purpose |
|------|---------|
| `start.bat` | Start the application |
| `demo.bat` | Upload sample docs and test API |
| `verify-setup.bat` | Check if everything is set up correctly |
| `README.md` | Comprehensive documentation |
| `QUICKSTART.md` | Quick start guide |
| `API_DOCUMENTATION.md` | Complete API reference |
| `PROJECT_SUMMARY.md` | Project overview and features |
| `sample-conversation.json` | Example conversation with all features |

## 🌐 Web Interface

The web UI provides a ChatGPT-like experience:

- **Ask questions** in natural language
- **View answers** with source citations
- **See confidence scores** for each answer
- **Get instant responses** from cache (for repeated questions)
- **Beautiful UI** with modern design

Access it at: **http://localhost:8080**

## 🔧 Configuration

### Basic Configuration
Edit `src/main/resources/application.properties`:

```properties
# OpenAI API Key
spring.ai.openai.api-key=your-key-here

# Server Port
server.port=8080

# Confidence Threshold (0.0 - 1.0)
askbit.ai.confidence-threshold=0.7

# Max results to retrieve
askbit.ai.max-retrieval-results=5

# Enable PII redaction
askbit.ai.pii-redaction.enabled=true
```

### Advanced Configuration
- **Cache TTL**: Default 1 hour (3600 seconds)
- **Model Selection**: GPT-4 → GPT-3.5 → Cached
- **Document Storage**: `./documents` folder
- **Database**: H2 file-based (`./data/askbitdb`)

## 🐛 Troubleshooting

### Issue: "OPENAI_API_KEY not set"
**Solution**: Set the environment variable:
```bash
set OPENAI_API_KEY=sk-your-key
```

### Issue: "Port 8080 already in use"
**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

### Issue: "Build failed"
**Solution**: Check Java version:
```bash
java -version
```
You need Java 17 or higher.

### Issue: "Cannot upload documents"
**Solution**: Make sure the `documents` folder has write permissions.

### Issue: "No answers to questions"
**Solution**: 
1. Make sure documents are uploaded first
2. Wait a few seconds for indexing to complete
3. Check that OPENAI_API_KEY is valid

## 📊 Monitoring

### Check System Health
```bash
curl http://localhost:8080/api/v1/health
```

### View Metrics
```bash
curl http://localhost:8080/api/v1/admin/metrics
```

### View All Documents
```bash
curl http://localhost:8080/api/v1/documents
```

## 🎓 Learning Path

1. **Start with the web UI** - Get familiar with the interface
2. **Try the sample questions** - See how citations work
3. **Upload your own documents** - PDF, DOCX, or Markdown
4. **Use the API** - Integrate with your applications
5. **Check the metrics** - Monitor performance
6. **Read the docs** - Deep dive into features

## 📚 Documentation

- **[README.md](README.md)** - Full project documentation
- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute setup guide
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Architecture and features

## 🎨 Features Highlights

✅ **Grounded Answers** - Only uses company documents
✅ **Page-Level Citations** - Know exactly where information comes from
✅ **No Hallucinations** - Admits when it doesn't know
✅ **PII Protection** - Automatically redacts sensitive information
✅ **Lightning Fast Cache** - Repeated questions answered in <100ms
✅ **Smart Clarification** - Asks follow-up questions when needed
✅ **Beautiful UI** - Modern, responsive chat interface

## 🚀 Next Steps

After getting started:

1. **Upload your company policies** - Replace sample docs with real ones
2. **Customize the UI** - Edit `src/main/resources/static/index.html`
3. **Configure settings** - Tune confidence threshold, cache TTL, etc.
4. **Monitor metrics** - Track usage and performance
5. **Integrate with tools** - Use the API in your apps

## 💡 Tips

- **Ask specific questions** for best results
- **Upload important documents first** before asking questions
- **Check citations** to verify information
- **Use cache** - Popular questions are served instantly
- **Monitor metrics** - Track performance and usage

## 🎯 Success Metrics

AskBit.AI aims to:
- ⏱️ **Reduce query time** by 40-50%
- 📉 **Cut support tickets** significantly
- ✅ **Improve answer accuracy** with citations
- 😊 **Increase employee satisfaction**

## ❓ Need Help?

- Check the **README.md** for comprehensive documentation
- Review **API_DOCUMENTATION.md** for API details
- Look at **sample-conversation.json** for examples
- Run `verify-setup.bat` to check your configuration

## 🎉 You're Ready!

That's it! You're now ready to use AskBit.AI.

Start exploring by opening **http://localhost:8080** and asking your first question!

---

**Built with ❤️ for the AI Hackathon**

*"One tool. Thousands of hours saved. Fewer tickets. Faster answers. Happier teams."*

