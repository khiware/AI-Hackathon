# IT Security Policy v2.5

## Information Technology Security Policies

**Effective Date:** January 1, 2025  
**Version:** 2.5  
**Last Updated:** October 15, 2025

---

## 1. Password Policy

### 1.1 Password Requirements

All employees must follow these password requirements:
- **Minimum length:** 12 characters
- Must include: uppercase, lowercase, numbers, and special characters
- **Cannot** contain: username, common words, or company name
- Must be changed every **90 days**
- Cannot reuse last **5 passwords**

### 1.2 Multi-Factor Authentication (MFA)

MFA is **required** for:
- Email accounts
- VPN access
- Cloud services (AWS, Azure, GCP)
- HR and payroll systems
- Source code repositories

Recommended MFA methods:
1. Authentication app (preferred)
2. Hardware security key
3. SMS (fallback only)

### 1.3 Password Managers

Employees are encouraged to use approved password managers:
- 1Password (company-provided)
- LastPass
- Bitwarden

Company provides 1Password Business for all employees at no cost.

---

## 2. Device Security

### 2.1 Company-Issued Devices

All company laptops and devices must:
- Have **full-disk encryption** enabled
- Run approved antivirus software
- Be enrolled in Mobile Device Management (MDM)
- Have automatic updates enabled
- Be locked when unattended (max 5 minutes)

### 2.2 Personal Devices (BYOD)

If using personal devices for work:
- Must install company MDM profile
- Separate work profile required (Android/iOS)
- Company can remotely wipe **work data only**
- Must meet minimum security requirements

### 2.3 Lost or Stolen Devices

If a device is lost or stolen:
1. Report to IT Security immediately: security@company.com
2. IT will remotely lock/wipe the device
3. File police report (provide copy to IT)
4. Change passwords for all accounts accessed from device

---

## 3. Data Classification and Handling

### 3.1 Data Classification Levels

**Public:**
- Marketing materials
- Public website content
- Press releases

**Internal:**
- Internal communications
- General business documents
- Non-sensitive project information

**Confidential:**
- Customer data
- Financial information
- Business strategies
- Employee information

**Restricted:**
- Trade secrets
- Legal documents
- Security credentials
- Personally Identifiable Information (PII)

### 3.2 Data Handling Requirements

**Confidential and Restricted data must:**
- Be encrypted at rest and in transit
- Only be shared via approved secure channels
- Never be stored on personal devices
- Be deleted when no longer needed
- Not be sent via unencrypted email

### 3.3 Data Retention

- **Email:** 7 years
- **Financial records:** 10 years
- **Employee records:** 7 years after termination
- **Customer data:** Per contract or legal requirement
- **Logs:** 1 year minimum

---

## 4. Network Security

### 4.1 VPN Usage

VPN is **required** when:
- Accessing company resources from home
- Using public Wi-Fi
- Traveling internationally
- Accessing sensitive systems

Approved VPN clients:
- Cisco AnyConnect (primary)
- OpenVPN (fallback)

### 4.2 Wi-Fi Security

**Office Wi-Fi:**
- Corporate network requires certificate authentication
- Guest network available for visitors

**Remote work:**
- Use WPA3 or WPA2 encryption minimum
- Change default router passwords
- Disable WPS (Wi-Fi Protected Setup)
- Use separate network for IoT devices

### 4.3 Firewall Requirements

All company devices must:
- Have firewall enabled
- Block incoming connections by default
- Only allow necessary outbound connections

---

## 5. Email and Communication Security

### 5.1 Email Security

**Never:**
- Click suspicious links or attachments
- Reply to phishing emails
- Share credentials via email
- Forward company emails to personal accounts

**Always:**
- Verify sender before clicking links
- Use encrypted email for sensitive data
- Report phishing to: phishing@company.com
- Add external email warning tags

### 5.2 Instant Messaging

Approved messaging platforms:
- Slack (company workspace only)
- Microsoft Teams
- Zoom (for video calls)

**Not approved:**
- WhatsApp for company business
- Personal Slack workspaces
- Telegram
- Discord

### 5.3 Video Conferencing

When hosting meetings:
- Use waiting rooms for external participants
- Require passwords for sensitive meetings
- Don't share meeting links publicly
- Record only with participant consent
- End meetings properly (don't leave open)

---

## 6. Cloud Security

### 6.1 Approved Cloud Services

**Approved:**
- AWS (primary cloud provider)
- Google Workspace (email, docs)
- Microsoft 365 (office applications)
- GitHub (code repositories)
- Salesforce (CRM)

**Requires approval:**
- Any new cloud service
- Sharing data with third parties
- Using personal cloud storage for work

### 6.2 Cloud Access

- Use company SSO (Single Sign-On) when available
- Enable MFA for all cloud services
- Review access permissions quarterly
- Revoke access when changing roles

### 6.3 Cloud Data Storage

**Sensitive data in cloud must:**
- Use server-side encryption
- Have access logging enabled
- Follow least-privilege access model
- Be in approved geographic regions (US, EU)

---

## 7. Incident Response

### 7.1 Security Incidents

Report immediately if you:
- Suspect malware infection
- Receive phishing email
- Lose a company device
- Accidentally share confidential data
- Notice unauthorized access
- Experience data breach

### 7.2 Reporting Process

**Critical incidents (within 1 hour):**
- Data breach
- Ransomware
- Unauthorized access to systems

**High priority (within 4 hours):**
- Suspected malware
- Lost/stolen devices
- Phishing attacks

**Normal priority (within 24 hours):**
- Policy violations
- General security questions

**Contact:**
- Email: security@company.com
- Phone: +1-555-SECURE (24/7)
- Emergency: Page on-call security engineer

### 7.3 Investigation

During security incidents:
- Do not turn off affected devices
- Disconnect from network if instructed
- Preserve evidence
- Cooperate with IT Security team
- Document what happened

---

## 8. Development Security

### 8.1 Code Repository Security

- All code in GitHub Enterprise
- Enable branch protection
- Require code reviews (minimum 2 reviewers)
- Run security scans on every commit
- No credentials in source code

### 8.2 API Keys and Secrets

- Use secret management tools (HashiCorp Vault, AWS Secrets Manager)
- Rotate keys every 90 days
- Never hardcode secrets
- Use environment variables
- Audit secret access

### 8.3 Third-Party Dependencies

- Scan for vulnerabilities weekly
- Update dependencies within 30 days of security patches
- Only use approved open-source licenses
- Document all dependencies

---

## 9. Physical Security

### 9.1 Office Access

- Badge required for office entry
- Don't tailgate or allow tailgating
- Report unknown persons to security
- Lock laptop when leaving desk
- Shred confidential documents

### 9.2 Clean Desk Policy

When leaving your desk:
- Lock computer screen
- Secure confidential documents
- Don't leave devices unattended
- Clear whiteboards with sensitive info

---

## 10. Training and Awareness

### 10.1 Required Training

All employees must complete:
- Security awareness training (annually)
- Phishing simulation tests (quarterly)
- Role-specific security training
- New hire security orientation

### 10.2 Security Champions

Each team should have a security champion who:
- Stays updated on security best practices
- Helps team members with security questions
- Participates in security working groups
- Reports concerns to IT Security

---

## 11. Compliance and Auditing

### 11.1 Security Audits

- Internal audits: Quarterly
- External audits: Annually
- Penetration testing: Twice per year
- Access reviews: Quarterly

### 11.2 Compliance Requirements

We comply with:
- SOC 2 Type II
- GDPR (for EU customers)
- CCPA (for California residents)
- HIPAA (for healthcare customers)
- PCI DSS (for payment processing)

---

## 12. Enforcement

### 12.1 Policy Violations

Violations may result in:
- Warning (first offense)
- Mandatory retraining
- Restricted access privileges
- Suspension
- Termination

Severity depends on:
- Intent (accidental vs. intentional)
- Impact (actual or potential)
- Cooperation with investigation
- Previous violations

### 12.2 Exceptions

Policy exceptions require:
- Written justification
- Manager approval
- IT Security approval
- Risk assessment
- Regular review (max 6 months)

---

## 13. Contact Information

**IT Security Team:**
- Email: security@company.com
- Phone: +1-555-SECURE (24/7)
- Slack: #security-help

**IT Help Desk:**
- Email: helpdesk@company.com
- Phone: +1-555-4357
- Portal: https://help.company.com

**Chief Information Security Officer (CISO):**
- Email: ciso@company.com

---

**Document ID:** IT-SEC-2025-v2.5  
**Approved by:** Chief Information Security Officer  
**Next Review:** April 2026
# HR Policy Document v3.1

## Company Human Resources Policies

**Effective Date:** January 1, 2025  
**Version:** 3.1  
**Last Updated:** November 1, 2025

---

## 1. Paid Time Off (PTO) Policy

### 1.1 Full-Time Employees - United States

Full-time employees in the United States are entitled to:
- **20 days** of paid time off per year
- **10 sick days** per year
- **5 personal days** per year

PTO accrues on a monthly basis at 1.67 days per month. New employees must wait 90 days before using accrued PTO.

### 1.2 Full-Time Employees - Europe

Full-time employees in European countries are entitled to:
- **25 days** of paid vacation per year (as per EU regulations)
- **15 sick days** per year
- **3 personal days** per year

### 1.3 Part-Time Employees

Part-time employees (working 20-35 hours per week) are entitled to PTO on a pro-rated basis:
- **12 days** of paid time off per year
- **6 sick days** per year

### 1.4 Contractors

Contractors are not eligible for paid time off. Please refer to your contract for specific terms.

---

## 2. Remote Work Policy

### 2.1 Full-Time Remote Work

Employees may request full-time remote work if:
- They have been with the company for at least 6 months
- Their role is suitable for remote work
- Their manager approves the request
- They are located in a country where the company has legal presence

### 2.2 International Remote Work

Employees may work remotely from another country for:
- **Up to 2 weeks** per year without special approval
- **Up to 3 months** per year with manager approval
- **More than 3 months** requires HR and legal approval

Tax implications and work permits must be considered for international remote work.

### 2.3 Hybrid Work

Most employees follow a hybrid schedule:
- **3 days in office** (Tuesday, Wednesday, Thursday)
- **2 days remote** (Monday, Friday)

Individual teams may adjust this schedule with manager approval.

---

## 3. Expense Reimbursement Policy

### 3.1 Travel Expenses

Employees can expense:
- **Flights**: Economy class for domestic, business class for international flights over 6 hours
- **Hotels**: Up to $200/night in US cities, €150/night in European cities
- **Meals**: Up to $50/day for domestic travel, €60/day for international travel
- **Ground transportation**: Taxis, ride-sharing, public transit

### 3.2 Equipment Expenses

**Full-Time Employees:**
- Laptop: Up to $2,000 (must be approved by IT)
- Monitor: Up to $500
- Keyboard/Mouse: Up to $150
- Headphones: Up to $300

**Contractors:**
- Laptop: Up to €800 (Germany), £700 (UK), $1,000 (US)
- Must get pre-approval from procurement

### 3.3 Training and Development

Employees can expense:
- Professional courses: Up to $2,000/year
- Conference tickets: Up to $1,500/year
- Books and learning materials: Up to $500/year

All training must be job-related and pre-approved by manager.

---

## 4. Health and Benefits

### 4.1 Health Insurance - United States

Full-time US employees receive:
- **Medical insurance**: Company pays 80% of premium
- **Dental insurance**: Company pays 60% of premium
- **Vision insurance**: Company pays 50% of premium
- Coverage starts on first day of employment

### 4.2 Health Insurance - International

European employees receive health insurance according to local regulations and company policy.

### 4.3 Retirement Benefits

**401(k) - United States:**
- Company matches 100% up to 6% of salary
- Vesting: Immediate

**Pension Plans - Europe:**
- Varies by country, aligned with local regulations

---

## 5. Parental Leave

### 5.1 Maternity Leave

- **16 weeks** paid maternity leave
- Can be taken before or after birth
- Additional unpaid leave available (up to 6 months)

### 5.2 Paternity Leave

- **8 weeks** paid paternity leave
- Must be taken within 6 months of birth

### 5.3 Adoption Leave

- **12 weeks** paid adoption leave
- Same benefits as biological parents

---

## 6. Work Hours and Overtime

### 6.1 Standard Work Hours

- **40 hours per week** for full-time employees
- Core hours: 10 AM - 3 PM (must be available)
- Flexible start time: 7 AM - 10 AM

### 6.2 Overtime

Non-exempt employees receive:
- **1.5x pay** for hours over 40 per week
- **2x pay** for work on holidays

Overtime must be pre-approved by manager.

---

## 7. Performance Reviews

### 7.1 Annual Review

- Conducted annually in Q4
- Includes self-assessment and manager evaluation
- Salary adjustments effective January 1

### 7.2 Probation Period

- New employees: 90-day probation period
- Review at end of probation
- Can be extended by 30 days if needed

---

## 8. Code of Conduct

All employees must:
- Maintain professional behavior
- Respect diversity and inclusion
- Follow data privacy and security policies
- Report violations to HR

Violations may result in disciplinary action up to and including termination.

---

## 9. Contact Information

**HR Department:**
- Email: hr@company.com
- Phone: +1-555-0100
- Office Hours: Monday-Friday, 9 AM - 5 PM EST

**For Policy Questions:**
- Submit ticket: https://help.company.com
- HR Portal: https://hr.company.com

---

**Document ID:** HR-POL-2025-v3.1  
**Approved by:** Chief People Officer  
**Next Review:** June 2026

