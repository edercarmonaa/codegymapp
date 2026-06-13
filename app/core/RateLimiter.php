<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class RateLimiter
{
    public static function enforce(): void
    {
        if (PHP_SAPI === 'cli' || !(bool) Env::get('RATE_LIMIT_ENABLED', true)) {
            return;
        }

        $limit = max(10, (int) Env::get('RATE_LIMIT_REQUESTS', 120));
        $window = max(10, (int) Env::get('RATE_LIMIT_WINDOW_SECONDS', 60));
        $ip = self::clientIp();
        if ($ip === '') {
            return;
        }

        $directory = dirname(__DIR__, 2) . '/storage/cache/rate_limits';
        if (!is_dir($directory) && !mkdir($directory, 0755, true) && !is_dir($directory)) {
            return;
        }

        $file = $directory . '/' . hash('sha256', $ip) . '.json';
        $now = time();
        $state = ['window_start' => $now, 'count' => 0];

        $handle = fopen($file, 'c+');
        if ($handle === false) {
            return;
        }

        try {
            flock($handle, LOCK_EX);
            $contents = stream_get_contents($handle);
            $decoded = is_string($contents) && $contents !== '' ? json_decode($contents, true) : null;
            if (is_array($decoded)) {
                $state['window_start'] = (int) ($decoded['window_start'] ?? $now);
                $state['count'] = (int) ($decoded['count'] ?? 0);
            }

            if (($now - $state['window_start']) >= $window) {
                $state = ['window_start' => $now, 'count' => 0];
            }

            $state['count']++;
            ftruncate($handle, 0);
            rewind($handle);
            fwrite($handle, json_encode($state, JSON_THROW_ON_ERROR));
            fflush($handle);

            if ($state['count'] > $limit) {
                http_response_code(429);
                header('Retry-After: ' . max(1, $window - ($now - $state['window_start'])));
                exit('Too Many Requests');
            }
        } finally {
            flock($handle, LOCK_UN);
            fclose($handle);
        }
    }

    private static function clientIp(): string
    {
        $ip = (string) ($_SERVER['REMOTE_ADDR'] ?? '');
        return filter_var($ip, FILTER_VALIDATE_IP) ? $ip : '';
    }
}

if (!\class_exists('RateLimiter', false)) {
    \class_alias(RateLimiter::class, 'RateLimiter');
}
