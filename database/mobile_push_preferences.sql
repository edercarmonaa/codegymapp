ALTER TABLE mobile_device_tokens
    ADD COLUMN IF NOT EXISTS push_enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER is_active,
    ADD COLUMN IF NOT EXISTS reminder_time TIME NOT NULL DEFAULT '08:00:00' AFTER push_enabled;
