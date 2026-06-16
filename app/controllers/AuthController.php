<?php

declare(strict_types=1);

use CodeGymApp\Services\AuthService;

final class AuthController
{
    public function __construct(private readonly AuthService $authService = new AuthService())
    {
    }

    public function showLogin(): void
    {
        if (Auth::check()) {
            Response::redirect('/calendario');
        }

        View::render('auth/login', ['title' => 'Iniciar sesión'], 'auth');
    }

    public function login(): void
    {
        verify_csrf();

        $result = $this->authService->attemptLogin(
            (string) ($_POST['username'] ?? ''),
            (string) ($_POST['password'] ?? '')
        );

        if (!$result['ok']) {
            $this->fail($result['message'] ?? 'Usuario o contraseña incorrectos.');
        }

        if (!empty($result['regenerateSession'])) {
            session_regenerate_id(true);
            unset($_SESSION['csrf_token']);
        }
        Response::redirect('/calendario');
    }

    public function logout(): void
    {
        verify_csrf();
        $this->authService->logout(Auth::user());
        Response::redirect('/login');
    }

    private function fail(string $message): never
    {
        $_SESSION['flash_error'] = $message;
        Response::redirect('/login');
    }
}
