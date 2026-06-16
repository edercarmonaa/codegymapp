<?php

declare(strict_types=1);

use CodeGymApp\Services\GoalService;

final class GoalController
{
    public function __construct(private readonly GoalService $goalService = new GoalService())
    {
    }

    public function index(): void
    {
        $data = $this->goalService->indexPayload();
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('goals/table', $data);
            return;
        }
        View::render('goals/index', $data, 'main');
    }

    public function save(): void
    {
        verify_csrf();

        $result = $this->goalService->create($_POST);
        if (!$result['ok']) {
            $_SESSION['flash_error'] = $result['message'] ?? 'No se pudo crear la meta.';
            Response::redirect('/metas');
        }

        $_SESSION['flash_success'] = 'Meta creada correctamente.';
        Response::redirect('/metas');
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->goalService->deactivate((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Meta desactivada.';
        Response::redirect('/metas');
    }
}
