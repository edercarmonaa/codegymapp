<?php

declare(strict_types=1);

function valid_theme(mixed $theme): string
{
    $theme = (string) $theme;
    return in_array($theme, ['light', 'dark'], true) ? $theme : 'light';
}

function shared_theme_cookie(): string
{
    return valid_theme($_COOKIE['codegym_theme'] ?? ($_COOKIE['codegym_public_theme'] ?? 'light'));
}

function current_web_theme(?array $user = null): string
{
    return valid_theme($_COOKIE['codegym_theme'] ?? ($user['preferred_theme'] ?? ($_COOKIE['codegym_public_theme'] ?? 'light')));
}

function remember_web_theme(string $theme): void
{
    $theme = valid_theme($theme);
    foreach (['codegym_theme', 'codegym_public_theme'] as $cookieName) {
        setcookie($cookieName, $theme, [
            'expires' => time() + 31536000,
            'path' => '/',
            'secure' => (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off'),
            'httponly' => false,
            'samesite' => 'Lax',
        ]);
        $_COOKIE[$cookieName] = $theme;
    }
}
