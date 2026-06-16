<?php

declare(strict_types=1);

use CodeGymApp\Services\PlatformService;

final class PlatformController
{
    public function __construct(private readonly PlatformService $platformService = new PlatformService())
    {
    }

    public function index(): void
    {
        $data = $this->platformService->indexPayload();
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('platforms/table', $data);
            return;
        }
        View::render('platforms/index', $data, 'main');
    }

    public function save(): void
    {
        verify_csrf();
        $result = $this->platformService->save($_POST);
        if (!$result['ok']) {
            $_SESSION['flash_error'] = $result['message'] ?? 'No se pudo guardar la plataforma.';
        }
        Response::redirect('/plataformas');
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->platformService->setActive((int) ($_POST['id'] ?? 0), false);
        Response::redirect('/plataformas');
    }

    public function activate(): void
    {
        verify_csrf();
        $this->platformService->setActive((int) ($_POST['id'] ?? 0), true);
        Response::redirect('/plataformas');
    }
}
