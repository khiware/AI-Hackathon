# Admin Dashboard - Quick Reference Guide

## 🎯 Access the Dashboard

**URL**: http://localhost:8080/admin.html

Or click the "🛠️ Admin Dashboard" button in the top-right corner of the main chat interface.

---

## 📊 Dashboard Sections

### 1. METRICS OVERVIEW (Top of page)
```
┌──────────────┬──────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
│ ⚡ P95 Latency│ 🎯 Cache Hit │ 💰 Model Cost│ ❓ Clarify   │ 🔒 PII       │ 📊 Total     │
│   125ms      │   Rate 78.5% │   $0.045     │   Rate 12.3% │   Redacted 5 │   Queries 23 │
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
```

### 2. TOP QUESTIONS TAB
View the most frequently asked questions with analytics:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📈 Top Questions                                         [Refresh Button]  │
├────────────────────────────────┬───────────┬──────────────┬────────────────┤
│ Question                       │ Frequency │ Avg Confidence│ Last Asked     │
├────────────────────────────────┼───────────┼──────────────┼────────────────┤
│ What is the vacation policy?   │    15     │    0.92      │ Nov 8, 2:30 PM │
│ How do I reset my password?    │    12     │    0.88      │ Nov 8, 1:45 PM │
│ What are the WFH guidelines?   │    10     │    0.85      │ Nov 8, 11:20 AM│
└────────────────────────────────┴───────────┴──────────────┴────────────────┘
```

### 3. DOCUMENT MANAGEMENT TAB
Upload and manage knowledge base documents:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📄 Upload New Document                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                              📁                                             │
│                   Click to upload or drag and drop                          │
│              Supports PDF, DOCX, TXT, MD files                              │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ Version: [1.0          ]                                                    │
│ Description: [Brief description of the document        ]                   │
│                                                                             │
│ [Upload Document]                                                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 📚 Existing Documents                                    [Refresh Button]  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 📄 HR_Policy_v3.1.md                                              [🗑️]    │
│    Version 3.1 • Uploaded Nov 1, 2025 • 45 chunks                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 📄 IT_Security_Policy_v2.5.md                                     [🗑️]    │
│    Version 2.5 • Uploaded Oct 28, 2025 • 38 chunks                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4. CACHE MANAGEMENT TAB
Control and monitor the response cache:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 🗄️ Cache Management                                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ Clear the response cache to force fresh answers for all questions.         │
│ This will temporarily increase response times and API costs.               │
│                                                                             │
│ [Clear All Cache]  [View Cache Stats]                                      │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ Cache Statistics:                                                           │
│                                                                             │
│ ┌────────────────┬────────────────┐                                        │
│ │ Cache Size     │ Hit Rate       │                                        │
│ │   23 entries   │   78.5%        │                                        │
│ └────────────────┴────────────────┘                                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5. ANALYTICS TAB
Detailed system analytics and performance metrics:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📊 Detailed Analytics                                                       │
├──────────────┬──────────────┬──────────────┬──────────────┬────────────────┤
│ Avg Response │ Avg          │ Total        │ Total        │ Most Used      │
│ Time         │ Confidence   │ Documents    │ Chunks       │ Model          │
│ 95ms         │ 0.87         │ 12           │ 456          │ gpt-3.5-turbo  │
└──────────────┴──────────────┴──────────────┴──────────────┴────────────────┘
```

---

## 🔧 Common Actions

### Refresh Metrics
Click the "🔄 Refresh" button in the top navigation bar to reload all metrics.

### Upload a Document
1. Go to "Document Management" tab
2. Click the upload area or drag & drop a file
3. Enter version and description (optional)
4. Click "Upload Document"
5. Wait for success confirmation

### Clear Cache
1. Go to "Cache Management" tab
2. Click "Clear All Cache" button
3. Confirm the action in the dialog
4. Cache will be cleared immediately

### View Top Questions
1. Go to "Top Questions" tab
2. See the most frequently asked questions
3. Click "Refresh" to update the list
4. Use this data to improve your documentation

### Delete a Document
1. Go to "Document Management" tab
2. Find the document in "Existing Documents" list
3. Click the 🗑️ (trash) icon
4. Confirm deletion
5. Document and its chunks will be removed

---

## 📱 Responsive Design

The dashboard is fully responsive and works on:
- ✅ Desktop (1920x1080 and above)
- ✅ Laptop (1366x768 and above)
- ✅ Tablet (768x1024)
- ✅ Mobile (375x667 and above)

---

## 🎨 Color Coding

- **Purple Gradient** = Primary actions and headers
- **Green Badge** = Successful operations
- **Yellow Badge** = Warnings or pending items
- **Red Badge** = Errors or critical items
- **Blue Badge** = Informational items

---

## ⚡ Quick Tips

1. **Monitor Performance**: Keep an eye on P95 latency - it should stay under 500ms
2. **Optimize Cache**: Aim for a cache hit rate above 70%
3. **Track Costs**: Monitor model usage costs to stay within budget
4. **Improve Content**: Use top questions to identify gaps in documentation
5. **Manage Documents**: Keep documents up-to-date by uploading new versions
6. **Clear Cache Wisely**: Only invalidate cache after major document updates

---

## 🔗 Navigation

- **Back to Chat**: Click "Back to Chat" in the header to return to the main interface
- **Refresh**: Click "🔄 Refresh" to reload all dashboard data
- **Tab Switching**: Click any tab name to switch between sections

---

## ✨ Features at a Glance

| Feature | Status | Description |
|---------|--------|-------------|
| Real-time Metrics | ✅ | Live updates on system performance |
| Top Questions | ✅ | Analytics on most asked questions |
| Document Upload | ✅ | Easy file management with drag & drop |
| Cache Control | ✅ | Invalidate cache when needed |
| Cost Tracking | ✅ | Monitor API usage costs |
| PII Monitoring | ✅ | Track data protection metrics |
| Performance Stats | ✅ | P95 latency and response times |

---

**Need Help?** All actions show confirmation dialogs and success/error messages to guide you through the process.

