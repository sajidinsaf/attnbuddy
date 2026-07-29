# AttnBuddy — Database Schema

## Phase 1 Tables

### user
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Login email |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hash (strength 12) |
| display_name | VARCHAR(100) | NOT NULL | User's display name |
| context | ENUM('EXECUTIVE','PROFESSIONAL','STUDENT') | NOT NULL | User context for templates/tone |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() ON UPDATE | |

### task
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK → user.id, NOT NULL | Task owner |
| title | VARCHAR(500) | NOT NULL | Task title |
| notes | TEXT | NULLABLE | Optional details |
| urgency | ENUM('URGENT','NOT_URGENT') | NOT NULL, DEFAULT 'NOT_URGENT' | Eisenhower urgency |
| importance | ENUM('IMPORTANT','NOT_IMPORTANT') | NOT NULL, DEFAULT 'IMPORTANT' | Eisenhower importance |
| status | ENUM('PENDING','DONE','SKIPPED','SNOOZED') | NOT NULL, DEFAULT 'PENDING' | Current status |
| due_date | TIMESTAMP | NULLABLE | Optional deadline |
| snoozed_until | TIMESTAMP | NULLABLE | Snooze expiry (NULL = not snoozed) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| completed_at | TIMESTAMP | NULLABLE | When marked done |

**Indexes:**
- `idx_task_user_status` on (user_id, status) — primary query path for /tasks/now
- `idx_task_user_due` on (user_id, due_date) — deadline-based queries
- `idx_task_snoozed` on (snoozed_until) — snooze expiry checks

## Phase 2 Tables

### micro_step
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| task_id | BIGINT | FK → task.id, NOT NULL | Parent task |
| title | VARCHAR(500) | NOT NULL | Step description |
| position | INT | NOT NULL | Display order |
| status | ENUM('PENDING','DONE') | NOT NULL, DEFAULT 'PENDING' | |
| created_at | TIMESTAMP | NOT NULL | |
| completed_at | TIMESTAMP | NULLABLE | |

### task_template / template_step
Pre-built decomposition templates (see REQUIREMENTS.md P2-04).

### time_box_session
Time-boxed focus sessions with duration tracking.

## Phase 3 Tables

### push_token
Expo push notification tokens per device.

### nudge_preference
Per-user nudge frequency and quiet hours.

### nudge_log
History of sent nudges (for deduplication and learning).

## Phase 4 Tables

### context_breadcrumb
Notes left when switching tasks ("where was I?").

### energy_log
User energy level check-ins by time.

### daily_pattern
Aggregated completion data by hour-of-day (for pattern learning).

### goal
Long-term goals with progress tracking.

## Phase 5 Tables

### focus_session
Body doubling sessions with check-in intervals.

### rescue_event
Rescue mode activations and resolutions.

## Migration Strategy

Using Flyway for all schema changes:
- Migrations in `backend/src/main/resources/db/migration/`
- Naming: `V{phase}_{sequence}__{description}.sql` (e.g., `V1_001__create_user_table.sql`)
- All migrations are forward-only (no down migrations)
- Tested against H2 in dev, MySQL in production
