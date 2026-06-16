<?php

declare(strict_types=1);

use CodeGymApp\Services\GoalService;

final class ApiGoalController
{
    public function __construct(private readonly GoalService $goalService = new GoalService())
    {
    }

    public function list(): void
    {
        $data = $this->goalService->indexPayload();
        Response::json([
            'ok' => true,
            'goals' => array_map([$this, 'goalResource'], $data['goals']),
            'pagination' => $data['pagination'],
            'goalTypes' => $data['goalTypes'],
            'periodTypes' => $data['periodTypes'],
        ]);
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

    /** @param array<string, mixed> $goal @return array<string, mixed> */
    private function goalResource(array $goal): array
    {
        return [
            'id' => (int) ($goal['id'] ?? 0),
            'goal_type' => (string) ($goal['goal_type'] ?? ''),
            'period_type' => (string) ($goal['period_type'] ?? ''),
            'target_value' => (int) ($goal['target_value'] ?? 0),
            'current_value' => (int) ($goal['current_value'] ?? 0),
            'progress_percent' => (float) ($goal['progress_percent'] ?? 0),
            'platform_name' => (string) ($goal['platform_name'] ?? ''),
            'language_name' => (string) ($goal['language_name'] ?? ''),
            'period_start' => (string) ($goal['period_start'] ?? ''),
            'period_end' => (string) ($goal['period_end'] ?? ''),
            'status' => (string) ($goal['status'] ?? ''),
            'auto_renew' => (int) ($goal['auto_renew'] ?? 0) === 1,
        ];
    }
}
