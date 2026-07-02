CREATE TABLE IF NOT EXISTS mobile_device_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(30) NOT NULL DEFAULT 'android',
    device_name VARCHAR(120) NULL,
    app_version VARCHAR(40) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    push_enabled TINYINT(1) NOT NULL DEFAULT 1,
    reminder_time TIME NOT NULL DEFAULT '08:00:00',
    last_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY mobile_device_tokens_token_unique (token),
    KEY mobile_device_tokens_user_active_index (user_id, is_active),
    CONSTRAINT mobile_device_tokens_user_id_foreign
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
