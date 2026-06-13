<?php

declare(strict_types=1);

final class Auth
{
    public const COOKIE = 'codegymapp_token';

    /** @return array<string, mixed>|null */
    public static function user(): ?array
    {
        $token = $_COOKIE[self::COOKIE] ?? '';
        if (!is_string($token) || $token === '') {
            return null;
        }

        $payload = Jwt::decode($token);
        if ($payload === null) {
            self::logoutCookie();
            return null;
        }

        return User::find((int) $payload['sub']);
    }

    public static function check(): bool
    {
        return self::user() !== null;
    }

    /** @param array<string, mixed> $user */
    public static function login(array $user): void
    {
        $minutes = (int) Env::get('JWT_EXPIRES_MINUTES', 30);
        $token = Jwt::encode([
            'sub' => (int) $user['id'],
            'username' => (string) $user['username'],
            'iat' => time(),
            'exp' => time() + ($minutes * 60),
        ]);

        setcookie(self::COOKIE, $token, [
            'expires' => time() + ($minutes * 60),
            'path' => '/',
            'secure' => (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off'),
            'httponly' => true,
            'samesite' => 'Lax',
        ]);
    }

    public static function logoutCookie(): void
    {
        setcookie(self::COOKIE, '', [
            'expires' => time() - 3600,
            'path' => '/',
            'secure' => (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off'),
            'httponly' => true,
            'samesite' => 'Lax',
        ]);
    }
}

