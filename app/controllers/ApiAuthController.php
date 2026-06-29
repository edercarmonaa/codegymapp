<?php

declare(strict_types=1);

use CodeGymApp\Services\AuthService;

final class ApiAuthController
{
    public function __construct(private readonly AuthService $authService = new AuthService())
    {
    }

    public function login(): void
    {
        $input = $this->input();
        $result = $this->authService->attemptLogin(
            (string) ($input['username'] ?? ''),
            (string) ($input['password'] ?? ''),
            false,
            true,
            (string) ($input['device_name'] ?? '')
        );

        if (!$result['ok']) {
            http_response_code(401);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'Usuario o contraseña incorrectos.',
            ]);
            return;
        }

        Response::json([
            'ok' => true,
            'token' => $result['token'],
            'expires_in' => $result['expires_in'],
            'refresh_token' => $result['refresh_token'] ?? null,
            'refresh_expires_in' => $result['refresh_expires_in'] ?? null,
            'user' => $this->publicUser($result['user'] ?? []),
        ]);
    }

    public function refresh(): void
    {
        $input = $this->input();
        $result = $this->authService->refreshMobileSession((string) ($input['refresh_token'] ?? ''));

        if (!$result['ok']) {
            http_response_code(401);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'No se pudo renovar la sesión.',
            ]);
            return;
        }

        Response::json([
            'ok' => true,
            'token' => $result['token'],
            'expires_in' => $result['expires_in'],
            'refresh_token' => $result['refresh_token'] ?? null,
            'refresh_expires_in' => $result['refresh_expires_in'] ?? null,
            'user' => $this->publicUser($result['user'] ?? []),
        ]);
    }

    public function revokeRefresh(): void
    {
        $input = $this->input();
        $this->authService->revokeMobileRefreshToken((string) ($input['refresh_token'] ?? ''));

        Response::json([
            'ok' => true,
            'message' => 'Refresh token revocado.',
        ]);
    }

    /** @return array<string, mixed> */
    private function input(): array
    {
        $contentType = (string) ($_SERVER['CONTENT_TYPE'] ?? '');
        if (str_contains(strtolower($contentType), 'application/json')) {
            $payload = json_decode(file_get_contents('php://input') ?: '{}', true);
            return is_array($payload) ? $payload : [];
        }

        return $_POST;
    }

    /** @param array<string, mixed> $user @return array<string, mixed> */
    private function publicUser(array $user): array
    {
        return [
            'id' => (int) ($user['id'] ?? 0),
            'username' => (string) ($user['username'] ?? ''),
            'name' => (string) ($user['name'] ?? ''),
            'email' => (string) ($user['email'] ?? ''),
        ];
    }
}
