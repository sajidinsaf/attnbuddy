-- Focus session tracking for time-boxed work sessions
CREATE TABLE focus_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    duration_minutes INT NOT NULL,
    actual_minutes INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    CONSTRAINT fk_focus_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_focus_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_focus_user_date ON focus_session(user_id, started_at);
CREATE INDEX idx_focus_task ON focus_session(task_id);
