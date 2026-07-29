---
name: issue
description: Create a well-structured GitHub issue for tracking work. Use when the user wants to create, file, or log a new issue.
allowed-tools: "Bash(gh *) Read Grep"
---

# Issue — Create a GitHub Issue

Create a well-structured GitHub issue for tracking work.

## Instructions

Parse $ARGUMENTS for the issue details. If not provided, ask the user.

### Step 1: Gather Details
- **Title**: Short, imperative (e.g., "Add product search endpoint")
- **Type**: feature | bug | refactor | test | docs | chore
- **Description**: What needs to be done and why
- **Acceptance Criteria**: Bullet list of what "done" looks like

### Step 2: Create the Issue
Use `gh issue create` with:
- Title
- Body formatted as:
```
## Description
{description}

## Type
{type}

## Acceptance Criteria
- [ ] {criterion 1}
- [ ] {criterion 2}
...

## Files Likely Affected
- {file paths}
```
- Label matching the type

### Step 3: Report
Display the issue number and URL.

### Important Rules
- Every issue must have clear acceptance criteria
- Issues should be scoped to a single logical change
- Large features should be broken into multiple issues
