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
        $password = (string) ($_POST['password'] ?? '');
        $errors = password_policy_errors($password);
        if (!$user || $errors) {
            $_SESSION['flash_error'] = implode(' ', $errors);
            Response::redirect('/usuario');
        }

        User::updatePassword((int) $user['id'], password_hash($password, PASSWORD_DEFAULT));
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
        Response::redirect($_SERVER['HTTP_REFERER'] ?? '/dashboard');
    }
}

