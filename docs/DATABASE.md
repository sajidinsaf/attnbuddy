# Brasstacks — Database Schema

## Phase 1 Tables

### user
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Login email |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hash (strength 12) |
| display_name | VARCHAR(100) | NOT NULL | User's display name |
| profile | ENUM('EXECUTIVE','PROFESSIONAL','STUDENT') | NOT NULL | Profile for seeding default domains/templates |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() ON UPDATE | |

### life_domain
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK → user.id, NOT NULL | Owner |
| name | VARCHAR(100) | NOT NULL | e.g., "Work", "Family", "Personal" |
| color | VARCHAR(7) | NOT NULL, DEFAULT '#6366F1' | Hex color for UI |
| weight | INT | NOT NULL, DEFAULT 50 | Priority weight (1-100) |
| active_start | TIME | NULLABLE | Preferred start time (e.g., 09:00) |
| active_end | TIME | NULLABLE | Preferred end time (e.g., 18:00) |
| position | INT | NOT NULL | Display order |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

**Default domains seeded at registration:**

| Profile | Seeded Domains |
|---------|---------------|
| Executive | Work (weight 70), Family (50), Personal (40), Financial (50), Health (40) |
| Professional | Work (60), Personal (50), Health (40) |
| Student | Study (70), Personal (50), Health (40) |

Users can add, rename, reorder, and adjust weights at any time.

### task
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | FK → user.id, NOT NULL | Task owner |
| domain_id | BIGINT | FK → life_domain.id, NULLABLE | Life domain (NULL = uncategorized) |
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
- `idx_task_domain` on (domain_id) — domain-filtered queries

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
