CREATE TABLE life_domain (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#6366F1',
    weight INT NOT NULL DEFAULT 50,
    active_start TIME,
    active_end TIME,
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_domain_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);
