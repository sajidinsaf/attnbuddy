# AttnBuddy — Architecture

## System Overview

```
┌─────────────────────────────────────────────┐
│              Mobile App (Expo)              │
│         React Native + TypeScript           │
│              iOS + Android                  │
└──────────────────┬──────────────────────────┘
                   │ HTTPS
                   ▼
┌──────────────────────────────────────────────┐
│         api.visibleai.com                    │
│     Spring Boot REST API (Tomcat)            │
│                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ Auth     │ │ Task     │ │ Prioritiza-  │ │
│  │ (JWT)    │ │ Service  │ │ tion Engine  │ │
│  └──────────┘ └──────────┘ └──────────────┘ │
│                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ Nudge    │ │ Pattern  │ │ AI Adapter   │ │
│  │ Scheduler│ │ Learner  │ │ (Phase 6)    │ │
│  └──────────┘ └──────────┘ └──────────────┘ │
└───────┬──────────────┬───────────────────────┘
        │              │
   ┌────▼────┐    ┌────▼────┐
   │  MySQL  │    │  Redis  │
   └─────────┘    └─────────┘
```

## Tech Stack

| Layer | Technology | Justification |
|-------|-----------|---------------|
| Mobile | React Native + Expo | Cross-platform iOS/Android, Expo simplifies build/deploy for dev with no mobile experience |
| Backend | Spring Boot 3.x | Developer's primary expertise, proven in production on MochaHost |
| Database | MySQL | Available on MochaHost, relational model fits task/user data |
| Cache | Redis | Available on MochaHost, used for session caching and nudge job queues |
| Auth | JWT (access + refresh) | Stateless auth suitable for mobile clients |
| Push | Expo Push Notifications | Unified APNs + FCM through single HTTP API |
| Deployment | MochaHost shared Tomcat | Existing paid infrastructure |

## Domain Model

```
┌──────────┐       ┌──────────┐
│   User   │──────<│   Task   │
│          │  1:N  │          │
└──────────┘       └────┬─────┘
                        │ 1:N
                   ┌────▼──────┐
                   │ MicroStep │ (Phase 2)
                   └───────────┘
```

## Authentication Flow

```
Client                          Server
  │                                │
  │─── POST /auth/register ──────>│
  │<── 201 + access + refresh ────│
  │                                │
  │─── GET /tasks/now ────────────>│
  │    Authorization: Bearer {at}  │
  │<── 200 + task ─────────────────│
  │                                │
  │─── POST /auth/refresh ────────>│
  │    { refreshToken }            │
  │<── 200 + new access token ─────│
```

- Access tokens: 1 hour expiry
- Refresh tokens: 30 day expiry, stored in Redis, single-use (rotation)
- Passwords: BCrypt (strength 12)

## Prioritization Engine

Rule-based algorithm, no AI/LLM dependency:

```
Score = EisenhowerBase + DeadlineBonus + StalenessBonus
        + DomainWeight + DomainTimeMatch
        + EnergyMatch + GoalAlignment

EisenhowerBase:
  Q1 (Urgent + Important)     = 100
  Q2 (Not Urgent + Important) = 70
  Q3 (Urgent + Not Important) = 50
  Q4 (Neither)                = 20

DeadlineBonus:
  Due within 24h = +30
  Due within 72h = +15

StalenessBonus:
  Pending > 3 days = +10

DomainWeight (Phase 1):
  Normalized domain weight (1-100) mapped to 0-20 bonus
  e.g., Work domain weight 70 → +14

DomainTimeMatch (Phase 1):
  Current time within domain's active_start..active_end → +15
  Current time outside domain's active window → -10

EnergyMatch (Phase 4):
  Current hour is user's high-energy time AND task is Important = +20

GoalAlignment (Phase 4):
  Task linked to active goal = +10

Exclusions:
  - Snoozed until future time
  - Status != PENDING

Return: highest-scoring pending task
```

## Privacy Architecture

**Core principle**: Personal data NEVER leaves the user's device or our controlled server.

```
┌─────────────────────────────────────────┐
│              TRUST BOUNDARY             │
│                                         │
│  Mobile Device    ←→    Our Server      │
│  (Expo app)             (MochaHost)     │
│                                         │
│  All personal data stays within this    │
│  boundary. Nothing crosses to third     │
│  party services.                        │
└─────────────────────────────────────────┘
         │
         ✕ BLOCKED: No personal data to cloud AI/LLMs
         ✕ BLOCKED: No personal data to analytics services
         ✕ BLOCKED: No personal data to third-party APIs
```

**Phase 6 AI safeguards** (when AI is added):
- `AiAdapter` interface — vendor-neutral, pluggable
- `DataClassifier` service — scans outbound text, blocks if personal data detected
- Rescue mode data is ALWAYS private — never processed by any AI
- Cost controls: daily token budget, rate limits, kill switch
- AI features opt-in, off by default

## Subdomain Plan

| Subdomain | Purpose |
|-----------|---------|
| `api.visibleai.com` | Spring Boot REST API |
| `visibleai.com` | Marketing/landing page (future) |

## Push Notification Architecture

```
Spring Boot (NudgeScheduler)
  │
  │ @Scheduled (every 15 min)
  │ Evaluate nudge rules per user
  │
  ▼
Expo Push API (https://exp.host/--/api/v2/push/send)
  │
  ├──> APNs (iOS)
  └──> FCM (Android)
```

No personal task content in push notification payloads visible to Expo — notifications contain generic prompts ("You have a priority task waiting") with task details fetched on-device when the notification is opened.
