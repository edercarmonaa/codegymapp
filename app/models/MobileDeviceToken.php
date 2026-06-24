<?php

declare(strict_types=1);

final class MobileDeviceToken extends BaseModel
{
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
                is_active, last_seen_at, created_at, updated_at
            ) VALUES (
                :user_id, :token, :platform, :device_name, :app_version,
                1, NOW(), NOW(), NOW()
            )
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                platform = VALUES(platform),
                device_name = VALUES(device_name),
                app_version = VALUES(app_version),
                is_active = 1,
                last_seen_at = NOW(),
                updated_at = NOW()"
        );

        return $stmt->execute([
            'user_id' => $userId,
            'token' => $token,
            'platform' => self::cleanShortText($data['platform'] ?? 'android', 30),
            'device_name' => self::nullableText($data['device_name'] ?? null, 120),
            'app_version' => self::nullableText($data['app_version'] ?? null, 40),
        ]);
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
}
