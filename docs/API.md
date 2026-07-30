# Brasstacks — REST API Reference

Base URL: `https://api.visibleai.com`

All endpoints return JSON. Authentication via Bearer token in `Authorization` header.

---

## Authentication

### POST /api/auth/register

Create a new user account.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "displayName": "Sajid",
  "context": "EXECUTIVE"
}
```

**Response (201):**
```json
{
  "userId": 1,
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600
}
```

### POST /api/auth/login

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200):**
```json
{
  "userId": 1,
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600
}
```

### POST /api/auth/refresh

**Request:**
```json
{
  "refreshToken": "eyJ..."
}
```

**Response (200):**
```json
{
  "accessToken": "eyJ...",
  "expiresIn": 3600
}
```

---

## Tasks

All task endpoints require authentication.

### POST /api/tasks

Quick-capture a new task.

**Request:**
```json
{
  "title": "Review Q3 financials",
  "notes": "Focus on revenue variance",
  "urgency": "URGENT",
  "importance": "IMPORTANT",
  "dueDate": "2026-08-01T17:00:00Z"
}
```

Only `title` is required. All other fields are optional with sensible defaults:
- `urgency`: defaults to `NOT_URGENT`
- `importance`: defaults to `IMPORTANT`

**Response (201):**
```json
{
  "id": 42,
  "title": "Review Q3 financials",
  "urgency": "URGENT",
  "importance": "IMPORTANT",
  "status": "PENDING",
  "dueDate": "2026-08-01T17:00:00Z",
  "createdAt": "2026-07-29T10:00:00Z"
}
```

### GET /api/tasks/now

Get the single highest-priority task to work on right now.

**Response (200):**
```json
{
  "task": {
    "id": 42,
    "title": "Review Q3 financials",
    "notes": "Focus on revenue variance",
    "urgency": "URGENT",
    "importance": "IMPORTANT",
    "status": "PENDING",
    "dueDate": "2026-08-01T17:00:00Z",
    "createdAt": "2026-07-29T10:00:00Z",
    "score": 130
  },
  "pendingCount": 7
}
```

**Response (200, no tasks):**
```json
{
  "task": null,
  "pendingCount": 0
}
```

### POST /api/tasks/{id}/done

Mark task as completed.

**Response (200):**
```json
{
  "id": 42,
  "status": "DONE",
  "completedAt": "2026-07-29T11:30:00Z"
}
```

### POST /api/tasks/{id}/skip

Skip task (temporarily deprioritize).

**Response (200):**
```json
{
  "id": 42,
  "status": "PENDING",
  "message": "Task deprioritized. Showing next task."
}
```

### POST /api/tasks/{id}/snooze

Snooze task until a specific time.

**Request:**
```json
{
  "until": "2026-07-29T14:00:00Z"
}
```

**Response (200):**
```json
{
  "id": 42,
  "status": "SNOOZED",
  "snoozedUntil": "2026-07-29T14:00:00Z"
}
```

### GET /api/tasks

List all tasks (debugging/admin, not used in primary UI).

**Query params:** `?status=PENDING&page=0&size=20`

**Response (200):**
```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0
}
```

---

## Error Responses

All errors follow this format:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Email is already registered",
  "timestamp": "2026-07-29T10:00:00Z"
}
```

| HTTP Status | Error Code | Description |
|------------|------------|-------------|
| 400 | VALIDATION_ERROR | Invalid input |
| 401 | UNAUTHORIZED | Missing or invalid token |
| 403 | FORBIDDEN | Token valid but insufficient permissions |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Duplicate resource (e.g., email already registered) |
| 500 | INTERNAL_ERROR | Server error |
