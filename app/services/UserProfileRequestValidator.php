<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class UserProfileRequestValidator
{
    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string, data?: array<string, string>}
     */
    public function validate(array $input): array
    {
        $name = trim((string) ($input['name'] ?? ''));
        $username = trim((string) ($input['username'] ?? ''));
        $email = trim((string) ($input['email'] ?? ''));

        if ($name === '' || $username === '' || $email === '') {
            return ['ok' => false, 'message' => 'Completa nombre, usuario y correo.'];
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return ['ok' => false, 'message' => 'Captura un correo válido.'];
        }

        return [
            'ok' => true,
            'data' => [
                'name' => $name,
                'username' => $username,
                'email' => $email,
            ],
        ];
    }
}
