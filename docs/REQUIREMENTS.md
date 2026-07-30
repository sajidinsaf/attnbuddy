# Brasstacks — Requirements

## Product Vision

Brasstacks is a mobile app for people with ADHD that acts as an **executive function prosthetic**. Unlike traditional to-do lists and planners (which require the very executive functions ADHD impairs), Brasstacks actively manages the user through tasks — deciding what to do next, nudging at the right time, and adapting to individual patterns.

**Positioning**: Productivity and focus support app. NOT a medical device or therapeutic tool.

## Target Users

| Context | Description |
|---------|-------------|
| Executive | CEOs, founders, business leaders struggling with strategic vs. tactical prioritization |
| Professional | Knowledge workers managing competing responsibilities |
| Student | Students (secondary, university, graduate) managing academic workload |

---

## Phase 1: Foundation

| ID | Requirement | Status |
|----|-------------|--------|
| P1-01 | User can register with email, password, name, and context (executive/professional/student) | Done |
| P1-02 | User can log in and receive JWT access + refresh tokens | Done |
| P1-03 | User sees a single "Right Now" screen showing ONE task — not a list | Done |
| P1-04 | User can quick-capture a task with title and Eisenhower classification (urgency + importance) | Done |
| P1-05 | App automatically selects the highest-priority pending task using Eisenhower scoring + deadline proximity + staleness | Done |
| P1-06 | User can mark a task as Done, Skip, or Snooze from the Right Now screen | Done |
| P1-07 | Snoozed tasks are hidden until the snooze time expires | Done |
| P1-08 | Skipped tasks receive a temporary score penalty and rotate to the next task | Done |
| P1-09 | Context-specific task templates are seeded on registration (executive, student, professional) | Done |

## Phase 2: Time Perception & Task Decomposition

| ID | Requirement | Status |
|----|-------------|--------|
| P2-01 | User can start a time-boxed focus session with a visual countdown timer | Done |
| P2-02 | User receives a transition warning at 80% of the time-box duration | Done |
| P2-03 | User can break a task into micro-steps (< 2 min each) | Done |
| P2-04 | User can apply pre-built decomposition templates (academic: essay, exam, homework, presentation; executive: board prep, quarterly review, hiring, strategic planning) | Done |
| P2-05 | Right Now screen shows deadline countdown ("Due in 3 hours") | Done |
| P2-06 | User can view a simple history of completed tasks (momentum evidence) | Done |
| P2-07 | Parent task auto-completes when all micro-steps are done | Done |

## Phase 3: Active Nudging & Push Notifications

| ID | Requirement | Status |
|----|-------------|--------|
| P3-01 | App sends contextually intelligent push notifications (not dumb reminders) | Planned |
| P3-02 | Morning briefing notification: "Your 3 priorities today" | Planned |
| P3-03 | Deadline approaching notification | Planned |
| P3-04 | Staleness notification: "You haven't touched X in 5 days" | Planned |
| P3-05 | Gentle re-engagement: "One quick win for today: X" | Planned |
| P3-06 | User can configure quiet hours (no nudges) | Planned |
| P3-07 | User can set nudge frequency (eager / moderate / minimal) | Planned |

## Phase 4: Context Capture & Energy Patterns

| ID | Requirement | Status |
|----|-------------|--------|
| P4-01 | When switching tasks, user is prompted to leave a context breadcrumb | Planned |
| P4-02 | When returning to a task, the previous breadcrumb is shown | Planned |
| P4-03 | User can optionally log energy level (high / medium / low) | Planned |
| P4-04 | App tracks completion patterns by time of day (passive learning) | Planned |
| P4-05 | After 2+ weeks, prioritization engine uses learned energy patterns | Planned |
| P4-06 | Weekly review notification summarizing progress and slippage | Planned |
| P4-07 | User can set long-term goals and track progress | Planned |

## Phase 5: Body Doubling, Rescue Mode & Momentum

| ID | Requirement | Status |
|----|-------------|--------|
| P5-01 | Body doubling mode: focus session with periodic check-ins from the app | Planned |
| P5-02 | Rescue mode: "I'm overwhelmed" button that simplifies everything | Planned |
| P5-03 | Rescue mode offers: work 5 min on one thing, park it with a plan, or do grounding exercise | Planned |
| P5-04 | Momentum visualization: gentle, non-judgmental progress display | Planned |
| P5-05 | Micro-celebrations on task completion (varied, no streaks/badges) | Planned |
| P5-06 | Configurable tone (supportive / peer / playful) | Planned |

## Phase 6: AI Integration (Post-MVP)

| ID | Requirement | Status |
|----|-------------|--------|
| P6-01 | AI adapter interface: vendor-neutral, pluggable | Planned |
| P6-02 | On-device AI for task decomposition (NO cloud personal data) | Planned |
| P6-03 | Data classifier blocks personal data from leaving device/server | Planned |
| P6-04 | Cost controls: daily token budget, rate limits, kill switch | Planned |
| P6-05 | AI features are opt-in, off by default | Planned |

## Deferred: Family Accounts

| ID | Requirement | Status |
|----|-------------|--------|
| DF-01 | Parent creates family group and invites children | Deferred |
| DF-02 | Child accounts exist only under a parent account | Deferred |
| DF-03 | Parental visibility configurable per child (summary / full) | Deferred |
| DF-04 | Parent can assign tasks to child's queue | Deferred |
| DF-05 | Parent can configure quiet hours for child | Deferred |
| DF-06 | Parental summary: weekly digest of child's patterns | Deferred |

**Reason deferred**: App Store review complications. Parental control features trigger stricter Apple review. Core app value doesn't depend on family accounts. Children (13+) can use app as regular users initially.

---

## Non-Functional Requirements

| ID | Requirement | Status |
|----|-------------|--------|
| NF-01 | NEVER send personal user data to any cloud/remote AI provider or LLM | Hard rule |
| NF-02 | AI/LLM integration must be vendor-neutral (interface/adapter pattern) | Hard rule |
| NF-03 | App is NOT a medical device — never make therapeutic claims | Hard rule |
| NF-04 | No addictive patterns: no infinite scroll, no engagement-maximizing dark patterns | Hard rule |
| NF-05 | No social comparison between users | Hard rule |
| NF-06 | No punishment for missed tasks — never show "overdue" in red | Hard rule |
| NF-07 | User can always pause all nudges (right to disconnect) | Hard rule |
| NF-08 | App must work offline for core features (view current task, capture new tasks) | Planned |
| NF-09 | Rescue mode data is ALWAYS private — never shared, never sent to AI | Hard rule |
