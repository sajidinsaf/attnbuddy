---
name: code-review
description: |
  Comprehensive code review for backend and frontend code.
  Catches bugs, improves quality, ensures security and best practices.
  Use when: reviewing code changes, PR reviews, architecture reviews, security audits.
---

# Code Review Excellence

Transform code reviews from gatekeeping to knowledge sharing through constructive feedback and systematic analysis.

## Core Principles

- Catch bugs and edge cases
- Ensure code maintainability
- Enforce coding standards
- Verify security (especially sensitive data handling)
- Check domain-specific compliance requirements

## Review Checklist

### Backend

1. **Data Access**: N+1 queries, missing transactions, lazy loading issues, missing indexes
2. **Security**: Proper password hashing, encryption for sensitive data, no secrets in logs, proper authorization checks
3. **Validation**: Input validation on request DTOs, schema validation matching backend constraints
4. **Error Handling**: Proper exception hierarchy, global error handler coverage, no stack traces in responses
5. **REST API**: Consistent naming, proper HTTP methods/status codes, pagination for lists
6. **Async**: Proper async patterns, no blocking calls in async methods
7. **Testing**: Service layer unit tests, controller integration tests, security tests

### Frontend

1. **Type Safety**: No `any` types, proper interface definitions, schema validation
2. **State Management**: Proper store design, no unnecessary re-renders, proper selector usage
3. **Forms**: Proper validation with error display and loading states
4. **API Calls**: Proper error handling, loading indicators, auth token refresh
5. **Security**: No sensitive data in localStorage, XSS prevention, CSRF tokens

## Output Format

For each issue:
```
**[SEVERITY]: [title]**
File: path/to/file:line
[Description and fix suggestion]
```

Order by severity: CRITICAL > HIGH > MEDIUM > LOW
