<?php

declare(strict_types=1);

use CodeGymApp\Services\LanguageService;

final class ApiLanguageController
{
    public function __construct(private readonly LanguageService $languageService = new LanguageService())
    {
    }

    public function save(): void
    {
        verify_csrf();
        $result = $this->languageService->save($_POST);
        $this->respond($result, 'Lenguaje guardado correctamente.');
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->languageService->setActive((int) ($_POST['id'] ?? 0), false);
        $this->respond(['ok' => true], 'Lenguaje desactivado.');
    }

    public function activate(): void
    {
        verify_csrf();
        $this->languageService->setActive((int) ($_POST['id'] ?? 0), true);
        $this->respond(['ok' => true], 'Lenguaje activado.');
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
