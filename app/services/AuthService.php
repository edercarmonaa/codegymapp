<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class AuthService
{
    /** @return array{ok: bool, message?: string, user?: array<string, mixed>, token?: string, expires_in?: int, refresh_token?: string, refresh_expires_in?: int, regenerateSession?: bool} */
    public function attemptLogin(string $username, string $password, bool $issueCookie = true, bool $issueRefreshToken = false, ?string $deviceName = null): array
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
        $token = \Auth::tokenForUser($user);
        if ($issueCookie) {
            \Auth::login($user);
        }
        \SecurityLog::record((int) $user['id'], 'login_success', 'success', 'Inicio de sesión exitoso.');

        $result = [
            'ok' => true,
            'user' => $user,
            'token' => $token,
            'expires_in' => \Auth::tokenTtlSeconds(),
            'regenerateSession' => true,
        ];

        if ($issueRefreshToken) {
            $refresh = \MobileRefreshToken::issue((int) $user['id'], $deviceName);
            $result['refresh_token'] = $refresh['token'];
            $result['refresh_expires_in'] = $refresh['expires_in'];
            \SecurityLog::record((int) $user['id'], 'mobile_refresh_issued', 'success', 'Refresh token móvil emitido.');
        }

        return $result;
    }

    /** @return array{ok: bool, message?: string, user?: array<string, mixed>, token?: string, expires_in?: int, refresh_token?: string, refresh_expires_in?: int} */
    public function refreshMobileSession(string $refreshToken): array
    {
        $refresh = \MobileRefreshToken::rotate($refreshToken);
        if (!$refresh['ok']) {
            \SecurityLog::record(null, 'mobile_refresh_failed', 'failure', $refresh['message'] ?? 'Refresh token inválido.');
            return ['ok' => false, 'message' => $refresh['message'] ?? 'No se pudo renovar la sesión.'];
        }

        $user = $refresh['user'] ?? [];
        $token = \Auth::tokenForUser($user);
        \SecurityLog::record((int) ($user['id'] ?? 0), 'mobile_refresh_success', 'success', 'Sesión móvil renovada.');

        return [
            'ok' => true,
            'user' => $user,
            'token' => $token,
            'expires_in' => \Auth::tokenTtlSeconds(),
            'refresh_token' => $refresh['refresh_token'],
            'refresh_expires_in' => $refresh['refresh_expires_in'],
        ];
    }

    public function revokeMobileRefreshToken(string $refreshToken): bool
    {
        $revoked = \MobileRefreshToken::revoke($refreshToken);
        \SecurityLog::record(null, 'mobile_refresh_revoked', $revoked ? 'success' : 'warning', 'Refresh token móvil revocado.');
        return $revoked;
    }

    /** @param array<string, mixed>|null $user */
    public function logout(?array $user): void
    {
        \SecurityLog::record($user ? (int) $user['id'] : null, 'logout', 'success', 'Cierre de sesión.');
        \Auth::logoutCookie();
    }
}
