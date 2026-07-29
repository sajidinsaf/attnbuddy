---
name: audit
description: Comprehensive audit of code quality, security, accessibility, and performance across the full stack.
user-invokable: true
args:
  - name: target
    description: The area or module to audit
    required: false
---

# Platform Audit

Systematic quality audit across the full stack.

## Audit Dimensions

### 1. Security (CRITICAL)
- Sensitive data encryption at rest and in transit
- Password hashing with strong algorithms
- Token lifecycle (expiry, refresh, revocation)
- CORS configuration
- CSRF protection
- Input sanitization (XSS, SQL injection)
- Audit logging completeness
- Secrets management (no hardcoded secrets)

### 2. Backend Quality
- Data model relationships and indexes
- Query efficiency (N+1 detection)
- Service layer transaction boundaries
- REST API consistency and documentation
- Error handling coverage
- Async processing patterns
- Database migration integrity

### 3. Frontend Quality
- Type safety (no `any` in TypeScript)
- Component composition and reuse
- Form validation completeness
- API error handling and user feedback
- Loading and empty states
- Responsive design (mobile, tablet, desktop)

### 4. Accessibility
- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation
- Color contrast ratios
- Screen reader compatibility
- Focus management in forms

### 5. Performance
- API response times
- Bundle size analysis
- Database query performance
- Caching strategy
- Lazy loading and code splitting

## Output

For each finding, provide:
- Severity (CRITICAL/HIGH/MEDIUM/LOW)
- Category (from dimensions above)
- Location (file:line)
- Description and remediation
