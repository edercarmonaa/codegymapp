<?php

declare(strict_types=1);

final class MobileRefreshToken extends BaseModel
{
    /** @return array{token: string, expires_in: int} */
    public static function issue(int $userId, ?string $deviceName = null): array
    {
        $token = bin2hex(random_bytes(64));
        $days = max(1, (int) Env::get('MOBILE_REFRESH_TOKEN_DAYS', 30));
        $expiresIn = $days * 24 * 60 * 60;
        $expiresAt = date('Y-m-d H:i:s', time() + $expiresIn);

        $stmt = self::db()->prepare(
            'INSERT INTO mobile_refresh_tokens (
                user_id, token_hash, device_name, expires_at, created_at, updated_at
            ) VALUES (
                :user_id, :token_hash, :device_name, :expires_at, NOW(), NOW()
            )'
        );
        $stmt->execute([
            'user_id' => $userId,
            'token_hash' => self::hash($token),
            'device_name' => self::nullableText($deviceName, 120),
            'expires_at' => $expiresAt,
        ]);

        return [
            'token' => $token,
            'expires_in' => $expiresIn,
        ];
    }

    /** @return array{ok: bool, user?: array<string, mixed>, refresh_token?: string, refresh_expires_in?: int, message?: string} */
    public static function rotate(string $token): array
    {
        $token = trim($token);
        if ($token === '') {
            return ['ok' => false, 'message' => 'Refresh token requerido.'];
        }

        $pdo = self::db();
        $pdo->beginTransaction();

        try {
            $stmt = $pdo->prepare(
                'SELECT mrt.*, u.username, u.name, u.email, u.is_active
                 FROM mobile_refresh_tokens mrt
                 INNER JOIN users u ON u.id = mrt.user_id
                 WHERE mrt.token_hash = :token_hash
                 LIMIT 1
                 FOR UPDATE'
            );
            $stmt->execute(['token_hash' => self::hash($token)]);
            $row = $stmt->fetch();

            if (!$row || !empty($row['revoked_at']) || strtotime((string) $row['expires_at']) <= time() || (int) $row['is_active'] !== 1) {
                $pdo->rollBack();
                return ['ok' => false, 'message' => 'Refresh token inválido o expirado.'];
            }

            $revoke = $pdo->prepare('UPDATE mobile_refresh_tokens SET revoked_at = NOW(), last_used_at = NOW(), updated_at = NOW() WHERE id = :id');
            $revoke->execute(['id' => (int) $row['id']]);

            $newToken = bin2hex(random_bytes(64));
            $days = max(1, (int) Env::get('MOBILE_REFRESH_TOKEN_DAYS', 30));
            $expiresIn = $days * 24 * 60 * 60;
            $expiresAt = date('Y-m-d H:i:s', time() + $expiresIn);

            $insert = $pdo->prepare(
                'INSERT INTO mobile_refresh_tokens (
                    user_id, token_hash, device_name, expires_at, created_at, updated_at
                ) VALUES (
                    :user_id, :token_hash, :device_name, :expires_at, NOW(), NOW()
                )'
            );
            $insert->execute([
                'user_id' => (int) $row['user_id'],
                'token_hash' => self::hash($newToken),
                'device_name' => self::nullableText($row['device_name'] ?? null, 120),
                'expires_at' => $expiresAt,
            ]);

            $pdo->commit();

            return [
                'ok' => true,
                'user' => [
                    'id' => (int) $row['user_id'],
                    'username' => (string) $row['username'],
                    'name' => (string) $row['name'],
                    'email' => (string) $row['email'],
                ],
                'refresh_token' => $newToken,
                'refresh_expires_in' => $expiresIn,
            ];
        } catch (Throwable $error) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            throw $error;
        }
    }

    public static function revoke(string $token): bool
    {
        $token = trim($token);
        if ($token === '') {
            return false;
        }

        $stmt = self::db()->prepare(
            'UPDATE mobile_refresh_tokens
             SET revoked_at = NOW(), updated_at = NOW()
             WHERE token_hash = :token_hash AND revoked_at IS NULL'
        );
        $stmt->execute(['token_hash' => self::hash($token)]);
        return $stmt->rowCount() > 0;
    }

    private static function hash(string $token): string
    {
        return hash('sha256', $token);
    }

    private static function nullableText(mixed $value, int $maxLength): ?string
    {
        $text = trim((string) $value);
        return $text === '' ? null : substr($text, 0, $maxLength);
    }
}
