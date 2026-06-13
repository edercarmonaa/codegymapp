<?php

declare(strict_types=1);

final class User extends BaseModel
{
    /** @return array<string, mixed>|null */
    public static function find(int $id): ?array
    {
        $stmt = self::db()->prepare('SELECT * FROM users WHERE id = :id AND is_active = 1 LIMIT 1');
        $stmt->execute(['id' => $id]);
        $user = $stmt->fetch();
        return is_array($user) ? $user : null;
    }

    /** @return array<string, mixed>|null */
    public static function findByUsername(string $username): ?array
    {
        $stmt = self::db()->prepare('SELECT * FROM users WHERE username = :username AND is_active = 1 LIMIT 1');
        $stmt->execute(['username' => $username]);
        $user = $stmt->fetch();
        return is_array($user) ? $user : null;
    }

    public static function countAll(): int
    {
        return (int) self::db()->query('SELECT COUNT(*) FROM users')->fetchColumn();
    }

    public static function updateLoginSuccess(int $id): void
    {
        $stmt = self::db()->prepare('UPDATE users SET failed_login_attempts = 0, locked_until = NULL, last_login_at = NOW(), updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id]);
    }

    public static function registerFailedAttempt(int $id): void
    {
        $stmt = self::db()->prepare('UPDATE users SET failed_login_attempts = failed_login_attempts + 1, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id]);
    }

    public static function lock(int $id): void
    {
        $minutes = (int) Env::get('LOGIN_BLOCK_MINUTES', 30);
        $lockedUntil = date('Y-m-d H:i:s', time() + ($minutes * 60));
        $stmt = self::db()->prepare('UPDATE users SET locked_until = :locked_until, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'locked_until' => $lockedUntil]);
    }

    /** @param array<string, string> $data */
    public static function updateProfile(int $id, array $data): void
    {
        $stmt = self::db()->prepare('UPDATE users SET name = :name, username = :username, email = :email, updated_at = NOW() WHERE id = :id');
        $stmt->execute([
            'id' => $id,
            'name' => $data['name'],
            'username' => $data['username'],
            'email' => $data['email'],
        ]);
    }

    public static function updateTheme(int $id, string $theme): void
    {
        $stmt = self::db()->prepare('UPDATE users SET preferred_theme = :theme, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'theme' => $theme]);
    }

    public static function updatePassword(int $id, string $hash): void
    {
        $stmt = self::db()->prepare('UPDATE users SET password_hash = :hash, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'hash' => $hash]);
    }
}
