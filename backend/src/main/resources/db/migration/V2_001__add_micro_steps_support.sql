-- Add parent_task_id for micro-step (subtask) support
ALTER TABLE task ADD COLUMN parent_task_id BIGINT NULL;
ALTER TABLE task ADD COLUMN position INT NOT NULL DEFAULT 0;

ALTER TABLE task ADD CONSTRAINT fk_task_parent
    FOREIGN KEY (parent_task_id) REFERENCES task(id) ON DELETE CASCADE;

CREATE INDEX idx_task_parent ON task(parent_task_id);
