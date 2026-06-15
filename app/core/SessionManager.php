<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class SessionManager
{
    private function __construct()
    {
    }

    public static function start(string $sessionName): void
    {
        if (session_status() === PHP_SESSION_ACTIVE) {
            return;
        }

        session_name($sessionName);
        session_set_cookie_params([
            'lifetime' => 0,
            'path' => '/',
            'secure' => self::isHttps(),
            'httponly' => true,
            'samesite' => 'Lax',
        ]);
        session_start();
    }

    private static function isHttps(): bool
    {
        return !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off';
    }
}

if (!\class_exists('SessionManager', false)) {
    \class_alias(SessionManager::class, 'SessionManager');
}
