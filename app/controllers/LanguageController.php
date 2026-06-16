<?php

declare(strict_types=1);

use CodeGymApp\Services\LanguageService;

final class LanguageController
{
    public function __construct(private readonly LanguageService $languageService = new LanguageService())
    {
    }

    public function index(): void
    {
        $data = $this->languageService->indexPayload();
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('languages/table', $data);
            return;
        }
        View::render('languages/index', $data, 'main');
    }

    public function save(): void
    {
        verify_csrf();
        $result = $this->languageService->save($_POST);
        if (!$result['ok']) {
            $_SESSION['flash_error'] = $result['message'] ?? 'No se pudo guardar el lenguaje.';
        }
        Response::redirect('/lenguajes');
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->languageService->setActive((int) ($_POST['id'] ?? 0), false);
        Response::redirect('/lenguajes');
    }

    public function activate(): void
    {
        verify_csrf();
        $this->languageService->setActive((int) ($_POST['id'] ?? 0), true);
        Response::redirect('/lenguajes');
    }
}
