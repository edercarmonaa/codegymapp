-- Ejecuta primero estas consultas para revisar si las columnas ya existen:
--
-- SELECT COLUMN_NAME
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME = 'mobile_device_tokens'
--   AND COLUMN_NAME IN ('push_enabled', 'reminder_time');
--
-- Si NO aparece push_enabled, ejecuta:
ALTER TABLE mobile_device_tokens
    ADD COLUMN push_enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER is_active;

-- Si NO aparece reminder_time, ejecuta:
ALTER TABLE mobile_device_tokens
    ADD COLUMN reminder_time TIME NOT NULL DEFAULT '08:00:00' AFTER push_enabled;

