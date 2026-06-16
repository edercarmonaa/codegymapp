<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class AuthService
{
    /** @return array{ok: bool, message?: string, user?: array<string, mixed>, regenerateSession?: bool} */
    public function attemptLogin(string $username, string $password): array
    {
        $username = trim($username);
        $user = \User::findByUsername($username);

        if (!$user) {
            \SecurityLog::record(null, 'login_failed', 'failure', 'Usuario inexistente: ' . $username);
            return ['ok' => false, 'message' => 'Usuario o contraseña incorrectos.'];
        }

        if (!empty($user['locked_until']) && strtotime((string) $user['locked_until']) > time()) {
            \SecurityLog::record((int) $user['id'], 'login_blocked', 'failure', 'Intento durante bloqueo temporal.');
            return ['ok' => false, 'message' => 'Tu usuario está bloqueado temporalmente. Intenta más tarde.'];
        }

        if (!password_verify($password, (string) $user['password_hash'])) {
            \User::registerFailedAttempt((int) $user['id']);
            \SecurityLog::record((int) $user['id'], 'login_failed', 'failure', 'Contraseña incorrecta.');
            $fresh = \User::find((int) $user['id']);
            if ($fresh && (int) $fresh['failed_login_attempts'] >= (int) \Env::get('LOGIN_MAX_ATTEMPTS', 3)) {
                \User::lock((int) $user['id']);
                \SecurityLog::record((int) $user['id'], 'login_locked', 'failure', 'Bloqueo por intentos fallidos.');
            }

            return ['ok' => false, 'message' => 'Usuario o contraseña incorrectos.'];
        }

        \User::updateLoginSuccess((int) $user['id']);
        \Auth::login($user);
        \SecurityLog::record((int) $user['id'], 'login_success', 'success', 'Inicio de sesión exitoso.');

        return [
            'ok' => true,
            'user' => $user,
            'regenerateSession' => true,
        ];
    }

    /** @param array<string, mixed>|null $user */
    public function logout(?array $user): void
    {
        \SecurityLog::record($user ? (int) $user['id'] : null, 'logout', 'success', 'Cierre de sesión.');
        \Auth::logoutCookie();
    }
}
