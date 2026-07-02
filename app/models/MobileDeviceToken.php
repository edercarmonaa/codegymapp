<?php

declare(strict_types=1);

final class MobileDeviceToken extends BaseModel
{
    /** @return array<int, int> */
    public static function activeUserIds(): array
    {
        $rows = self::db()
            ->query('SELECT DISTINCT user_id FROM mobile_device_tokens WHERE is_active = 1 AND push_enabled = 1 ORDER BY user_id ASC')
            ->fetchAll();

        return array_map('intval', array_column($rows, 'user_id'));
    }

    /** @return array<int, int> */
    public static function activeUserIdsForReminderTime(?string $time = null): array
    {
        $time = self::validReminderTime($time ?? date('H:i'));
        $stmt = self::db()->prepare(
            "SELECT DISTINCT user_id
             FROM mobile_device_tokens
             WHERE is_active = 1
               AND push_enabled = 1
               AND TIME_FORMAT(reminder_time, '%H:%i') = :reminder_time
             ORDER BY user_id ASC"
        );
        $stmt->execute(['reminder_time' => $time]);

        return array_map('intval', array_column($stmt->fetchAll(), 'user_id'));
    }

    /** @param array<string, mixed> $data */
    public static function upsert(int $userId, array $data): bool
    {
        $token = trim((string) ($data['token'] ?? ''));
        if ($userId <= 0 || $token === '' || strlen($token) > 512) {
            return false;
        }

        $stmt = self::db()->prepare(
            "INSERT INTO mobile_device_tokens (
                user_id, token, platform, device_name, app_version,
                is_active, push_enabled, reminder_time, last_seen_at, created_at, updated_at
            ) VALUES (
                :user_id, :token, :platform, :device_name, :app_version,
                1, :push_enabled, :reminder_time, NOW(), NOW(), NOW()
            )
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                platform = VALUES(platform),
                device_name = VALUES(device_name),
                app_version = VALUES(app_version),
                is_active = 1,
                push_enabled = VALUES(push_enabled),
                reminder_time = VALUES(reminder_time),
                last_seen_at = NOW(),
                updated_at = NOW()"
        );

        return $stmt->execute([
            'user_id' => $userId,
            'token' => $token,
            'platform' => self::cleanShortText($data['platform'] ?? 'android', 30),
            'device_name' => self::nullableText($data['device_name'] ?? null, 120),
            'app_version' => self::nullableText($data['app_version'] ?? null, 40),
            'push_enabled' => self::boolValue($data['push_enabled'] ?? true) ? 1 : 0,
            'reminder_time' => self::validReminderTime((string) ($data['reminder_time'] ?? '08:00')),
        ]);
    }

    /** @param array<string, mixed> $data */
    public static function updatePreferences(int $userId, array $data): bool
    {
        if ($userId <= 0) {
            return false;
        }

        $stmt = self::db()->prepare(
            "UPDATE mobile_device_tokens
             SET push_enabled = :push_enabled,
                 reminder_time = :reminder_time,
                 updated_at = NOW()
             WHERE user_id = :user_id"
        );
        $stmt->execute([
            'user_id' => $userId,
            'push_enabled' => self::boolValue($data['push_enabled'] ?? true) ? 1 : 0,
            'reminder_time' => self::validReminderTime((string) ($data['reminder_time'] ?? '08:00')),
        ]);

        return true;
    }

    private static function cleanShortText(mixed $value, int $maxLength): string
    {
        $text = trim((string) $value);
        return substr($text !== '' ? $text : 'android', 0, $maxLength);
    }

    private static function nullableText(mixed $value, int $maxLength): ?string
    {
        $text = trim((string) $value);
        return $text === '' ? null : substr($text, 0, $maxLength);
    }

    private static function boolValue(mixed $value): bool
    {
        if (is_bool($value)) {
            return $value;
        }

        return in_array(strtolower((string) $value), ['1', 'true', 'yes', 'on'], true);
    }

    private static function validReminderTime(string $time): string
    {
        $time = substr(trim($time), 0, 5);
        return preg_match('/^([01]\d|2[0-3]):[0-5]\d$/', $time) === 1 ? $time : '08:00';
    }
}
