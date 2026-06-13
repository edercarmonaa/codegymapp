<?php

declare(strict_types=1);

final class UserController
{
    public function index(): void
    {
        View::render('user/index', ['title' => 'Mi usuario', 'user' => Auth::user()], 'main');
    }

    public function update(): void
    {
        verify_csrf();
        $user = Auth::user();
        if ($user) {
            User::updateProfile((int) $user['id'], [
                'name' => trim((string) ($_POST['name'] ?? '')),
                'username' => trim((string) ($_POST['username'] ?? '')),
                'email' => trim((string) ($_POST['email'] ?? '')),
            ]);
        }
        Response::redirect('/usuario');
    }

    public function changePassword(): void
    {
        verify_csrf();
        $user = Auth::user();
        $currentPassword = (string) ($_POST['current_password'] ?? '');
        $password = (string) ($_POST['password'] ?? '');

        if (!$user || !password_verify($currentPassword, (string) ($user['password_hash'] ?? ''))) {
            SecurityLog::record($user ? (int) $user['id'] : null, 'password_change_failed', 'failure', 'Contraseña actual incorrecta.');
            $_SESSION['flash_error'] = 'La contraseña actual no es correcta.';
            Response::redirect('/usuario');
        }

        $errors = password_policy_errors($password);
        if ($errors) {
            $_SESSION['flash_error'] = implode(' ', $errors);
            Response::redirect('/usuario');
        }

        User::updatePassword((int) $user['id'], password_hash($password, PASSWORD_DEFAULT));
        session_regenerate_id(true);
        unset($_SESSION['csrf_token']);
        SecurityLog::record((int) $user['id'], 'password_changed', 'success', 'Cambio de contraseña.');
        $_SESSION['flash_success'] = 'Contraseña actualizada correctamente.';
        Response::redirect('/usuario');
    }

    public function changeTheme(): void
    {
        verify_csrf();
        $user = Auth::user();
        $theme = (string) ($_POST['theme'] ?? 'light');
        if ($user && in_array($theme, ['light', 'dark'], true)) {
            User::updateTheme((int) $user['id'], $theme);
        }
        $path = parse_url((string) ($_SERVER['HTTP_REFERER'] ?? ''), PHP_URL_PATH);
        Response::redirect(safe_app_url(is_string($path) ? $path : '', '/dashboard'));
    }
}
