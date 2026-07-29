# AttnBuddy — Design Decisions

This document records non-obvious architectural and product decisions with the reasoning behind each.

---

## DD-001: Executive Function Prosthetic, Not a To-Do List

**Decision**: The app shows ONE task at a time ("Right Now" screen). It never presents a scrollable list of tasks as the primary interface.

**Why**: Traditional to-do lists require prioritization (an executive function). People with ADHD experience decision paralysis when faced with multiple options. The app must do the deciding, not the user.

**Trade-off**: Power users may want to see "everything." We may add a secondary view for this but it must never be the default or primary interface.

---

## DD-002: Rule-Based Algorithms Over Cloud AI for MVP

**Decision**: The Prioritization Engine, nudging, and task suggestions use rule-based algorithms (Eisenhower matrix + deadline proximity + energy patterns), not LLM calls.

**Why**: Three reasons:
1. **Privacy**: This is a mental health support app. Sending personal task data to cloud AI providers is ethically unacceptable.
2. **Cost**: The developer experienced a $1,035 Claude API bill in a few hours. Cloud AI costs are unpredictable and dangerous.
3. **Reliability**: Rule-based algorithms are deterministic, fast, and work offline. LLMs are slow, expensive, and occasionally wrong.

**When this changes**: Phase 6 introduces an AI adapter layer with strict safeguards (vendor-neutral interface, data classification, cost controls, opt-in only).

---

## DD-003: Never Punish Missed Tasks

**Decision**: The app never shows tasks as "overdue" in red, never counts missed tasks, never breaks a streak. If a user misses a day, the app simply shows today's priority.

**Why**: ADHD is often accompanied by rejection sensitive dysphoria (RSD). Guilt-based motivation (streaks, red badges, overdue counts) creates anxiety and avoidance — the user stops opening the app entirely. This is the #1 reason ADHD users abandon productivity apps.

**Implementation**: No "overdue" status. Staleness is a scoring factor (older tasks rise in priority) but is never surfaced to the user as guilt.

---

## DD-004: Expo for Mobile Development

**Decision**: Use React Native with Expo rather than Flutter, Kotlin Multiplatform, or native Swift/Kotlin.

**Why**: The developer is a backend Java developer with zero mobile experience. Expo abstracts away Xcode, Android Studio, code signing, provisioning profiles, and native build systems. The development workflow is: edit TypeScript → see changes on physical phone instantly via Expo Go. This is critical for first-time mobile development.

**Trade-off**: Expo has limitations for advanced native features. If needed later, we can "eject" to bare React Native.

---

## DD-005: Family/Parental Features Deferred

**Decision**: Family group accounts, parental controls, and child account management are deferred to post-MVP.

**Why**: Apple's App Store review applies stricter scrutiny to apps with parent-manages-child features. This adds review friction, potential rejections, and delays. The core value of the app doesn't depend on family accounts. The developer's children (both 13+) can use the app as regular users initially.

**What we kept**: Student-specific features (academic templates, student onboarding context) are included since student ≠ child. These work for a 16-year-old and a 30-year-old graduate student equally.

---

## DD-006: Privacy as Architecture, Not Policy

**Decision**: Personal data protection is enforced at the architectural level (trust boundary, data classifier, blocked outbound paths), not just by policy or documentation.

**Why**: This is a mental health support app. Users share sensitive information about their struggles, daily patterns, and emotional states. A policy can be accidentally violated. An architectural boundary cannot — code that attempts to send personal data to an external service is blocked by the DataClassifier service before it leaves the trust boundary.

**Non-negotiable rules**:
- Rescue mode data is ALWAYS private — never shared, never sent to AI
- Push notification payloads contain generic text only — task details fetched on-device
- No third-party analytics that receives personal data
- If cloud AI is ever used, only fully anonymized generic prompts

---

## DD-007: Three User Contexts, Not Roles

**Decision**: Users select a "context" (Executive, Professional, Student) at registration. This is not a role/permission system — it controls which templates, default nudge styles, and prioritization hints are applied.

**Why**: The same ADHD challenges manifest differently in different life contexts. A CEO struggling to prioritize strategic work over daily fires needs different templates than a student struggling to start an essay. The context system lets us tailor the experience without building separate apps.

**Extensible**: New contexts can be added without schema changes (just new template sets and nudge copy).

---

## DD-008: Spring Boot Backend on MochaHost

**Decision**: Use Spring Boot 3.x deployed as WAR on existing MochaHost shared Tomcat.

**Why**: The developer has deep Spring Boot expertise and existing MochaHost infrastructure (Tomcat, MySQL, Redis, Node.js). Using existing paid infrastructure avoids new cloud service costs and leverages known deployment patterns (same as faridainsaf and irs1099 projects).

**Trade-off**: Shared hosting has resource limits. If the app scales significantly, migration to dedicated hosting or cloud will be needed.

---

## DD-009: Vendor-Neutral AI Adapter Pattern

**Decision**: All AI integration goes through an `AiAdapter` interface with pluggable implementations (LocalRuleAdapter, OnDeviceAdapter, CloudAnonAdapter).

**Why**: The developer was burned by a $1,035 Claude API bill. Vendor lock-in is a financial and strategic risk. The adapter pattern lets us:
- Start with rule-based algorithms (zero cost)
- Add on-device models later (zero marginal cost)
- Optionally use cloud AI with cost controls (if ever needed)
- Switch providers without changing business logic

---

## DD-010: Push Notifications as Core Value, Not Add-On

**Decision**: Push notifications are architecturally central (Phase 3), not a nice-to-have feature.

**Why**: The biggest ADHD challenge is task initiation — opening an app requires the very executive function the app is meant to compensate for. The app must come to the user, not wait for the user to come to it. Push notifications are the primary mechanism for this. Generic payloads protect privacy while still being actionable.
