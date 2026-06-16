<?php

declare(strict_types=1);

use CodeGymApp\Services\UserService;

final class UserController
{
    public function __construct(private readonly UserService $userService = new UserService())
    {
    }

    public function index(): void
    {
        View::render('user/index', $this->userService->pagePayload(), 'main');
    }

    public function update(): void
    {
        verify_csrf();
        $result = $this->userService->updateProfile(Auth::user(), $_POST);
        if ($result['ok']) {
            $_SESSION['flash_success'] = $result['message'] ?? 'Perfil actualizado correctamente.';
        } else {
            $_SESSION['flash_error'] = $result['message'] ?? 'No se pudo actualizar el perfil.';
        }
        Response::redirect('/usuario');
    }

    public function changePassword(): void
    {
        verify_csrf();
        $result = $this->userService->changePassword(Auth::user(), $_POST);
        if (!$result['ok']) {
            $_SESSION['flash_error'] = $result['message'];
            Response::redirect('/usuario');
        }

        if (!empty($result['regenerateSession'])) {
            session_regenerate_id(true);
            unset($_SESSION['csrf_token']);
        }
        $_SESSION['flash_success'] = $result['message'];
        Response::redirect('/usuario');
    }

    public function changeTheme(): void
    {
        verify_csrf();
        $theme = (string) ($_POST['theme'] ?? 'light');
        $this->userService->changeTheme(Auth::user(), $theme);
        $path = parse_url((string) ($_SERVER['HTTP_REFERER'] ?? ''), PHP_URL_PATH);
        Response::redirect(safe_app_url(is_string($path) ? $path : '', '/dashboard'));
    }
}
