<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class Auth
{
    public const COOKIE = 'codegymapp_token';

    /** @return array<string, mixed>|null */
    public static function user(): ?array
    {
        [$token, $source] = self::tokenFromRequest();
        if ($token === '') {
            return null;
        }

        $payload = Jwt::decode($token);
        if ($payload === null) {
            $error = Jwt::lastError();
            if ($error === 'expired') {
                \SecurityLog::record(null, 'session_expired', 'failure', 'Sesión expirada por tiempo.');
                $_SESSION['flash_error'] = 'Tu sesión expiró por seguridad. Inicia sesión nuevamente.';
            } else {
                \SecurityLog::record(null, 'token_invalid', 'failure', 'Token inválido, alterado o no legible.');
            }
            if ($source === 'cookie') {
                self::logoutCookie();
            }
            return null;
        }

        return \User::find((int) $payload['sub']);
    }

    public static function check(): bool
    {
        return self::user() !== null;
    }

    /** @param array<string, mixed> $user */
    public static function login(array $user): void
    {
        $minutes = (int) Env::get('JWT_EXPIRES_MINUTES', 30);
        $token = self::tokenForUser($user);

        self::setLoginCookie($token, $minutes);
    }

    /** @param array<string, mixed> $user */
    public static function tokenForUser(array $user): string
    {
        $minutes = (int) Env::get('JWT_EXPIRES_MINUTES', 30);

        return Jwt::encode([
            'sub' => (int) $user['id'],
            'username' => (string) $user['username'],
            'iat' => time(),
            'exp' => time() + ($minutes * 60),
        ]);
    }

    public static function tokenTtlSeconds(): int
    {
        return max(1, (int) Env::get('JWT_EXPIRES_MINUTES', 30)) * 60;
    }

    private static function setLoginCookie(string $token, int $minutes): void
    {
        setcookie(self::COOKIE, $token, [
            'expires' => time() + ($minutes * 60),
            'path' => '/',
            'secure' => self::isSecureRequest(),
            'httponly' => true,
            'samesite' => 'Strict',
        ]);
    }

    public static function logoutCookie(): void
    {
        setcookie(self::COOKIE, '', [
            'expires' => time() - 3600,
            'path' => '/',
            'secure' => self::isSecureRequest(),
            'httponly' => true,
            'samesite' => 'Strict',
        ]);
    }

    /** @return array{0: string, 1: string} */
    private static function tokenFromRequest(): array
    {
        $header = self::authorizationHeader();
        if (preg_match('/^Bearer\s+(.+)$/i', $header, $matches) === 1) {
            return [trim($matches[1]), 'bearer'];
        }

        $cookie = $_COOKIE[self::COOKIE] ?? '';
        return is_string($cookie) ? [$cookie, 'cookie'] : ['', ''];
    }

    private static function authorizationHeader(): string
    {
        $header = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';
        if (is_string($header) && $header !== '') {
            return trim($header);
        }

        if (function_exists('apache_request_headers')) {
            $headers = apache_request_headers();
            foreach ($headers as $name => $value) {
                if (strtolower((string) $name) === 'authorization') {
                    return trim((string) $value);
                }
            }
        }

        return '';
    }

    private static function isSecureRequest(): bool
    {
        if (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') {
            return true;
        }

        return strtolower((string) parse_url((string) Env::get('APP_URL', ''), PHP_URL_SCHEME)) === 'https';
    }
}

if (!\class_exists('Auth', false)) {
    \class_alias(Auth::class, 'Auth');
}
