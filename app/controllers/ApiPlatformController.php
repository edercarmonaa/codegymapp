<?php

declare(strict_types=1);

use CodeGymApp\Services\PlatformService;

final class ApiPlatformController
{
    public function __construct(private readonly PlatformService $platformService = new PlatformService())
    {
    }

    public function save(): void
    {
        verify_csrf();
        $result = $this->platformService->save($_POST);
        $this->respond($result, 'Plataforma guardada correctamente.');
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->platformService->setActive((int) ($_POST['id'] ?? 0), false);
        $this->respond(['ok' => true], 'Plataforma desactivada.');
    }

    public function activate(): void
    {
        verify_csrf();
        $this->platformService->setActive((int) ($_POST['id'] ?? 0), true);
        $this->respond(['ok' => true], 'Plataforma activada.');
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
