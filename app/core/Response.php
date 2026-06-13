<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class Response
{
    public static function redirect(string $path): never
    {
        header('Location: ' . $path);
        exit;
    }

    /** @param array<string, mixed> $payload */
    public static function json(array $payload): void
    {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
    }

    public static function htmxRedirect(string $path): void
    {
        header('HX-Redirect: ' . $path);
    }
}

if (!\class_exists('Response', false)) {
    \class_alias(Response::class, 'Response');
}
