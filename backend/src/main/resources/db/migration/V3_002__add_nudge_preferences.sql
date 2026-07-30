-- Nudge preferences on user
ALTER TABLE app_user ADD COLUMN nudge_frequency VARCHAR(20) NOT NULL DEFAULT 'MODERATE';
ALTER TABLE app_user ADD COLUMN quiet_start VARCHAR(5) DEFAULT NULL;
ALTER TABLE app_user ADD COLUMN quiet_end VARCHAR(5) DEFAULT NULL;
