<?php

declare(strict_types=1);

final class ApiMeController
{
    public function show(): void
    {
        $user = Auth::user();
        if (!$user) {
            http_response_code(401);
            Response::json(['ok' => false, 'message' => 'No autenticado.']);
            return;
        }

        Response::json([
            'ok' => true,
            'user' => [
                'id' => (int) ($user['id'] ?? 0),
                'username' => (string) ($user['username'] ?? ''),
                'name' => (string) ($user['name'] ?? ''),
                'email' => (string) ($user['email'] ?? ''),
                'preferred_theme' => (string) ($user['preferred_theme'] ?? 'light'),
                'last_login_at' => $user['last_login_at'] ?? null,
            ],
        ]);
    }
}
