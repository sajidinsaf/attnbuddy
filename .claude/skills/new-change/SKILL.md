---
name: new-change
description: SDLC Gate — ensures proper process before ANY code change. Use when starting new work, features, bugfixes, or refactors.
disable-model-invocation: false
allowed-tools: "Bash(gh *) Bash(git *) Read Grep"
---

# New Change — SDLC Gate

Before making ANY code change, this skill ensures proper SDLC process is followed.

## Instructions

You MUST follow these steps in order. Do NOT skip any step.

### Step 1: Define the Change
Ask the user (if not already provided via $ARGUMENTS):
- What is the change? (brief description)
- What type? (feature, bugfix, refactor, test, docs, chore)
- Which files/areas will be affected?

### Step 2: Create GitHub Issue
Create a GitHub issue using `gh issue create` with:
- Clear title (imperative mood, e.g., "Add product search endpoint")
- Body with: Description, Acceptance Criteria, Files to Change
- Appropriate labels (feature, bug, refactor, test, docs, chore)
- Assign to the current user

### Step 3: Create Feature Branch
Create a branch from main named: `{issue-number}-{short-description}`
Example: `42-add-product-search`

### Step 4: Confirm Ready
Display the issue URL and branch name. Confirm the user is ready to proceed with implementation.

### Important Rules
- NEVER make code changes without a GitHub issue
- NEVER commit to main directly
- Every commit message must reference the issue: `#{issue-number}`
- Every PR must reference the issue: `Closes #{issue-number}`
