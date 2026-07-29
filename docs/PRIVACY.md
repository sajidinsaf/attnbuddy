# AttnBuddy — Privacy Architecture

## Core Principle

**Personal data NEVER leaves the trust boundary** (user's device + our controlled server). This is a hard-wired architectural decision, not a policy that can be overridden.

## Why This Matters

AttnBuddy is a focus and productivity support app used by people with ADHD. Users will enter:
- Task descriptions that reveal personal, professional, and academic activities
- Emotional state data (rescue mode, energy levels)
- Daily patterns and routines
- Goals and aspirations

This data is deeply personal. Sending it to third-party cloud AI services, analytics platforms, or any external system is ethically unacceptable.

## Trust Boundary

```
┌─────────────────────────────────────────┐
│              TRUST BOUNDARY             │
│                                         │
│  Mobile Device    ←→    Our Server      │
│  (Expo app)             (MochaHost)     │
│                                         │
│  ✓ Task data stored in MySQL            │
│  ✓ Auth tokens in Redis                 │
│  ✓ All processing on our server         │
│  ✓ Push tokens stored for notifications │
│                                         │
└─────────────────────────────────────────┘
         │
         ✕ Cloud AI/LLM providers (Claude, OpenAI, Gemini, etc.)
         ✕ Third-party analytics (Mixpanel, Amplitude, etc.)
         ✕ Third-party crash reporting with user data
         ✕ Any external API that receives personal content
```

## Data Classification

| Data Type | Classification | Rules |
|-----------|---------------|-------|
| Task titles & notes | Personal | Never sent externally |
| Micro-step content | Personal | Never sent externally |
| Rescue mode data | Strictly Private | Never sent to AI, never shared between accounts |
| Energy levels | Personal | Never sent externally |
| Context breadcrumbs | Personal | Never sent externally |
| Completion timestamps | Behavioral | Used internally for pattern learning only |
| User email & name | PII | Never sent externally, not included in push payloads |
| Expo push token | Technical | Sent to Expo Push API (required for notifications) |
| Device info | Technical | Minimal collection, no fingerprinting |

## Push Notification Privacy

Push notifications are delivered through Expo's Push API, which routes to Apple (APNs) and Google (FCM). To protect privacy:

- Notification payloads contain **generic text only**: "You have a priority task waiting" / "Time for your morning briefing"
- **No task titles, descriptions, or personal content** in push payloads
- Task details are fetched on-device when the user opens the notification
- Push tokens are stored on our server (required for sending notifications)

## AI Integration Safeguards (Phase 6)

When AI capabilities are added:

1. **DataClassifier service**: Scans any text before it leaves the trust boundary. Blocks if personal data detected:
   - Proper nouns (names, places, organizations)
   - Contact information (email, phone)
   - Medical/health terms combined with identifiers
   - School or workplace names
   - Any rescue mode content (always blocked)

2. **Vendor-neutral adapter**: No direct vendor SDK imports in business logic. Switchable without code changes.

3. **Cost controls**: Daily token budget, per-user rate limits, global kill switch.

4. **Opt-in only**: AI features are disabled by default. User must explicitly enable.

5. **Anonymized prompts only**: If cloud AI is ever used, prompts are generic templates with no personal data. Example: "Break this into 5-minute steps: prepare a presentation" — never "Break this into steps: prepare the Q3 board presentation for Acme Corp investors."

## Data Retention

- Active user data: retained while account is active
- Deleted account: all personal data purged within 30 days
- Behavioral patterns (aggregated): anonymized and retained for product improvement
- Rescue mode events: user can delete at any time; auto-purged after 90 days

## Compliance Posture

While not legally required to comply with HIPAA (this is not a medical device), we adopt HIPAA-level data protection as a design principle because:
- The data is health-adjacent (ADHD management)
- Users trust us with their vulnerabilities
- It's the right thing to do
