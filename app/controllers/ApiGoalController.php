<?php

declare(strict_types=1);

use CodeGymApp\Services\GoalService;

final class ApiGoalController
{
    public function __construct(private readonly GoalService $goalService = new GoalService())
    {
    }

    public function save(): void
    {
        verify_csrf();
        $result = $this->goalService->create($_POST);
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'No se pudo crear la meta.',
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Meta creada correctamente.']);
    }

    public function deactivate(): void
    {
        verify_csrf();
        $this->goalService->deactivate((int) ($_POST['id'] ?? 0));
        Response::json(['ok' => true, 'message' => 'Meta desactivada.']);
    }
}
