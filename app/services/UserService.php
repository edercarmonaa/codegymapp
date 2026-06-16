<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class UserService
{
    public function __construct(private readonly UserProfileRequestValidator $profileValidator = new UserProfileRequestValidator())
    {
    }

    /** @return array<string, mixed> */
    public function pagePayload(): array
    {
        return [
            'title' => 'Mi usuario',
            'user' => \Auth::user(),
        ];
    }

    /**
     * @param array<string, mixed>|null $user
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string}
     */
    public function updateProfile(?array $user, array $input): array
    {
        if (!$user) {
            return ['ok' => false, 'message' => 'No se pudo identificar el usuario.'];
        }

        $validated = $this->profileValidator->validate($input);
        if (!$validated['ok']) {
            return ['ok' => false, 'message' => (string) $validated['message']];
        }

        \User::updateProfile((int) $user['id'], $validated['data']);
        return ['ok' => true, 'message' => 'Perfil actualizado correctamente.'];
    }

    /**
     * @param array<string, mixed>|null $user
     * @param array<string, mixed> $input
     * @return array{ok: bool, message: string, regenerateSession?: bool}
     */
    public function changePassword(?array $user, array $input): array
    {
        $currentPassword = (string) ($input['current_password'] ?? '');
        $password = (string) ($input['password'] ?? '');

        if (!$user || !password_verify($currentPassword, (string) ($user['password_hash'] ?? ''))) {
            \SecurityLog::record($user ? (int) $user['id'] : null, 'password_change_failed', 'failure', 'Contraseña actual incorrecta.');
            return ['ok' => false, 'message' => 'La contraseña actual no es correcta.'];
        }

        $errors = \password_policy_errors($password);
        if ($errors) {
            return ['ok' => false, 'message' => implode(' ', $errors)];
        }

        \User::updatePassword((int) $user['id'], password_hash($password, PASSWORD_DEFAULT));
        \SecurityLog::record((int) $user['id'], 'password_changed', 'success', 'Cambio de contraseña.');

        return [
            'ok' => true,
            'message' => 'Contraseña actualizada correctamente.',
            'regenerateSession' => true,
        ];
    }

    /** @param array<string, mixed>|null $user */
    public function changeTheme(?array $user, string $theme): void
    {
        if ($user && in_array($theme, ['light', 'dark'], true)) {
            \User::updateTheme((int) $user['id'], $theme);
        }
    }
}
