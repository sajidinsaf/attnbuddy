---
name: harden
description: Improve platform resilience through better error handling, input validation, edge case management, and security hardening.
user-invokable: true
args:
  - name: target
    description: The feature or area to harden
    required: false
---

# Platform Hardening

Strengthen the platform against edge cases, errors, and real-world usage.

## Backend Hardening

### Input Validation
- Validate all request DTOs at the boundary
- Custom validators for domain-specific formats
- Request size limits for file uploads
- Payload size validation

### Transaction Safety
- Transactions on all multi-table operations
- Optimistic locking on contested entities
- Idempotency keys for payment/external API calls
- Rollback on partial operation failures

### Error Resilience
- Retry with exponential backoff for external API calls
- Circuit breaker for external service calls
- Graceful degradation when dependencies are unavailable
- Rate limiting on auth endpoints (prevent brute force)

### Data Protection
- Never log sensitive data values
- Mask sensitive data in API responses
- Encrypt before database storage
- Audit log all sensitive data access

## Frontend Hardening

### Form Resilience
- Validation matching backend constraints
- Input masking for formatted fields
- Auto-save drafts to prevent data loss
- Confirmation dialogs before destructive actions

### Network Resilience
- Interceptor for auth token refresh
- Toast notifications for network errors
- Optimistic UI updates with rollback
- Offline detection and user notification

### Display Hardening
- Long text truncation with tooltips
- Number formatting for financial amounts
- Date formatting consistency
- Empty state handling for all data views
