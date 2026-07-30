-- Energy level on task completion + completion hour for pattern learning
ALTER TABLE task ADD COLUMN energy_level VARCHAR(10) DEFAULT NULL;
ALTER TABLE task ADD COLUMN completed_hour TINYINT DEFAULT NULL;
