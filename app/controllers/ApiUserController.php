<?php

declare(strict_types=1);

use CodeGymApp\Services\UserService;

final class ApiUserController
{
    public function __construct(private readonly UserService $userService = new UserService())
    {
    }

    public function update(): void
    {
        verify_csrf();
        $result = $this->userService->updateProfile(Auth::user(), $_POST);
        $this->respond($result, $result['message'] ?? 'Perfil actualizado correctamente.');
    }

    public function changePassword(): void
    {
        verify_csrf();
        $result = $this->userService->changePassword(Auth::user(), $_POST);
        if (!empty($result['regenerateSession'])) {
            session_regenerate_id(true);
            unset($_SESSION['csrf_token']);
        }

        $this->respond($result, $result['message'] ?? 'Contraseña actualizada correctamente.');
    }

    public function changeTheme(): void
    {
        verify_csrf();
        $theme = (string) ($_POST['theme'] ?? 'light');
        remember_web_theme($theme);
        $this->userService->changeTheme(Auth::user(), $theme);
        Response::json(['ok' => true, 'message' => 'Tema actualizado.']);
    }

    /** @param array{ok: bool, message?: string} $result */
    private function respond(array $result, string $successMessage): void
    {
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'No se pudo procesar la solicitud.',
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => $successMessage]);
    }
}
