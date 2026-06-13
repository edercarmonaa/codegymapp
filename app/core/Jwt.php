<?php

declare(strict_types=1);

final class Jwt
{
    private static ?string $lastError = null;

    /** @param array<string, mixed> $payload */
    public static function encode(array $payload): string
    {
        $header = ['typ' => 'JWT', 'alg' => 'HS256'];
        $segments = [
            self::base64UrlEncode(json_encode($header, JSON_THROW_ON_ERROR)),
            self::base64UrlEncode(json_encode($payload, JSON_THROW_ON_ERROR)),
        ];
        $signature = hash_hmac('sha256', implode('.', $segments), (string) Env::get('JWT_SECRET', ''), true);
        $segments[] = self::base64UrlEncode($signature);
        return implode('.', $segments);
    }

    /** @return array<string, mixed>|null */
    public static function decode(string $token): ?array
    {
        self::$lastError = null;
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            self::$lastError = 'invalid';
            return null;
        }

        [$header, $payload, $signature] = $parts;
        $expected = self::base64UrlEncode(hash_hmac('sha256', $header . '.' . $payload, (string) Env::get('JWT_SECRET', ''), true));
        if (!hash_equals($expected, $signature)) {
            self::$lastError = 'invalid';
            return null;
        }

        $data = json_decode(self::base64UrlDecode($payload), true);
        if (!is_array($data)) {
            self::$lastError = 'invalid';
            return null;
        }
        if ((int) ($data['exp'] ?? 0) < time()) {
            self::$lastError = 'expired';
            return null;
        }

        return $data;
    }

    public static function lastError(): ?string
    {
        return self::$lastError;
    }

    private static function base64UrlEncode(string $value): string
    {
        return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
    }

    private static function base64UrlDecode(string $value): string
    {
        return base64_decode(strtr($value, '-_', '+/')) ?: '';
    }
}
