-- Add sustained task fields to task table
ALTER TABLE task ADD COLUMN task_type ENUM('ONE_TIME', 'SUSTAINED') NOT NULL DEFAULT 'ONE_TIME';
ALTER TABLE task ADD COLUMN daily_effort_minutes INT NULL;
ALTER TABLE task ADD COLUMN target_date TIMESTAMP NULL;

-- Daily sessions for sustained tasks
CREATE TABLE daily_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    effort_minutes INT NOT NULL,
    minutes_logged INT NOT NULL DEFAULT 0,
    status ENUM('PENDING', 'DONE', 'SKIPPED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_session_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    UNIQUE INDEX idx_session_task_date (task_id, session_date),
    INDEX idx_session_user_date (user_id, session_date, status)
);
