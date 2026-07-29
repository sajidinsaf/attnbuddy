---
name: docs
description: Continuous documentation update. Use after any code change, design decision, or requirement update to keep docs in sync.
when_to_use: Run after code changes, design decisions, requirement updates, or infrastructure changes.
allowed-tools: "Read Grep Glob Edit Write Bash(git *)"
---

# Docs — Continuous Documentation Update

Ensure project documentation stays in sync with code and design changes. Run this skill after any code change, design decision, or requirement update.

## Instructions

### Step 1: Identify What Changed

Examine the current work — either from $ARGUMENTS, the recent git diff, or the conversation context. Classify the change:

- **Code change** — new feature, bug fix, refactor
- **Design change** — visual, UX, architecture decision
- **Requirement change** — new requirement, modified requirement, dropped requirement
- **Infrastructure change** — hosting, deployment, dependencies, observability

### Step 2: Determine Which Docs Need Updating

Check each doc for relevance to the change:

| Change Type | Docs to Check |
|-------------|---------------|
| New feature / endpoint | `API.md`, `ARCHITECTURE.md`, `REQUIREMENTS.md` |
| UI / design change | `DESIGN.md`, `REQUIREMENTS.md` |
| Database schema change | `DATABASE.md` |
| New dependency / config | `DEVELOPMENT.md`, `DEPLOYMENT.md` |
| Any code change | `CHANGELOG.md` |
| New/changed requirement | `REQUIREMENTS.md` |
| Architecture decision | `ARCHITECTURE.md` |

### Step 3: Read Current State of Affected Docs

Read each affected doc file from `docs/` to understand its current content before making changes.

### Step 4: Update the Docs

For each affected doc:

1. **Add** new sections/entries for new functionality
2. **Modify** existing sections if behavior changed
3. **Remove** outdated information that no longer applies
4. **Add decision records** for non-obvious choices: what was decided and *why*

For `REQUIREMENTS.md` specifically:
- Add new requirements with status (Planned, In Progress, Completed)
- Update requirement status when implementation progresses
- Mark requirements as Dropped with a reason if they are removed
- Never delete a requirement — mark it Dropped so there's a history

For `CHANGELOG.md`:
- Add an entry with date and description

### Step 5: Verify Consistency

Cross-check:
- Does `README.md` still accurately describe the project?
- Do the "quick start" instructions still work?
- Are all API endpoints documented?
- Does `REQUIREMENTS.md` reflect the latest agreed requirements?

### Step 6: Report

List which docs were updated and summarize what changed in each.

### Important Rules
- NEVER skip documentation updates when code changes
- Documentation updates should be in the SAME commit as the code change
- Write for a reader who hasn't seen the conversation — be self-contained
- Keep docs concise but complete — no filler text
- Use tables and bullet points for scannability
- Always include *why* for non-obvious decisions, not just *what*
- `REQUIREMENTS.md` is the source of truth for what the product should do
