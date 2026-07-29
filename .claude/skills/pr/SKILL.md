---
name: pr
description: Create a pull request with SDLC checks. Use when the user wants to create, open, or submit a PR.
disable-model-invocation: false
allowed-tools: "Bash(gh *) Bash(git *) Read Grep"
---

# Pull Request — Create PR with SDLC Checks

Create a pull request following project SDLC standards.

## Instructions

### Step 1: Pre-flight Checks
Before creating the PR, verify ALL of these pass:

1. **Tests pass**: Run the project's test suite — ALL tests must pass
2. **Build succeeds**: Run the project's build — no compilation errors
3. **Issue exists**: Verify there is a GitHub issue for this change
4. **Branch is correct**: Must NOT be on main/master
5. **Changes are committed**: No uncommitted changes

If any check fails, STOP and fix the issue before proceeding.

### Step 2: Create the PR
Use `gh pr create` with:
- Title matching the issue title
- Body containing:
  - `Closes #<issue-number>`
  - Summary of changes (bullet points)
  - Test plan (what was tested)
  - Checklist: tests pass, no TODOs left, docs updated if needed
- Link to the issue

### Step 3: Post-PR
- Display the PR URL
- Remind the user to review before merging

### Important Rules
- NEVER create a PR without passing tests
- NEVER create a PR without a linked issue
- PR title should be concise (<70 chars)
- PR body must include `Closes #N` to auto-close the issue on merge
