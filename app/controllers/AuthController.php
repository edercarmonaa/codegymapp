<?php

declare(strict_types=1);

final class AuthController
{
    public function showLogin(): void
    {
        if (Auth::check()) {
            Response::redirect('/dashboard');
        }

        View::render('auth/login', ['title' => 'Iniciar sesión'], 'auth');
    }

    public function login(): void
    {
        verify_csrf();

        $username = trim((string) ($_POST['username'] ?? ''));
        $password = (string) ($_POST['password'] ?? '');
        $user = User::findByUsername($username);

        if (!$user) {
            SecurityLog::record(null, 'login_failed', 'failure', 'Usuario inexistente: ' . $username);
            $this->fail('Usuario o contraseña incorrectos.');
        }

        if (!empty($user['locked_until']) && strtotime((string) $user['locked_until']) > time()) {
            SecurityLog::record((int) $user['id'], 'login_blocked', 'failure', 'Intento durante bloqueo temporal.');
            $this->fail('Tu usuario está bloqueado temporalmente. Intenta más tarde.');
        }

        if (!password_verify($password, (string) $user['password_hash'])) {
            User::registerFailedAttempt((int) $user['id']);
            SecurityLog::record((int) $user['id'], 'login_failed', 'failure', 'Contraseña incorrecta.');
            $fresh = User::find((int) $user['id']);
            if ($fresh && (int) $fresh['failed_login_attempts'] >= (int) Env::get('LOGIN_MAX_ATTEMPTS', 3)) {
                User::lock((int) $user['id']);
                SecurityLog::record((int) $user['id'], 'login_locked', 'failure', 'Bloqueo por intentos fallidos.');
            }
            $this->fail('Usuario o contraseña incorrectos.');
        }

        User::updateLoginSuccess((int) $user['id']);
        session_regenerate_id(true);
        unset($_SESSION['csrf_token']);
        Auth::login($user);
        SecurityLog::record((int) $user['id'], 'login_success', 'success', 'Inicio de sesión exitoso.');
        Response::redirect('/dashboard');
    }

    public function logout(): void
    {
        verify_csrf();
        $user = Auth::user();
        SecurityLog::record($user ? (int) $user['id'] : null, 'logout', 'success', 'Cierre de sesión.');
        Auth::logoutCookie();
        Response::redirect('/login');
    }

    private function fail(string $message): never
    {
        $_SESSION['flash_error'] = $message;
        Response::redirect('/login');
    }
}
