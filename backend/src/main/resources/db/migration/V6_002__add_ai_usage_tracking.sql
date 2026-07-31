-- AI usage tracking for cost controls
CREATE TABLE ai_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(20) NOT NULL,
    CONSTRAINT fk_ai_usage_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_usage_user_date ON ai_usage(user_id, used_at);
