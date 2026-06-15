<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class GoalRequestValidator
{
    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string, data?: array<string, mixed>}
     */
    public function validate(array $input): array
    {
        $target = (int) ($input['target_value'] ?? 0);
        if ($target <= 0) {
            return ['ok' => false, 'message' => 'Captura un objetivo mayor a cero.'];
        }

        return [
            'ok' => true,
            'data' => [
                'goal_type' => (string) ($input['goal_type'] ?? ''),
                'period_type' => (string) ($input['period_type'] ?? ''),
                'target_value' => $target,
                'platform_id' => (int) ($input['platform_id'] ?? 0),
                'language_id' => (int) ($input['language_id'] ?? 0),
                'auto_renew' => isset($input['auto_renew']) ? 1 : 0,
            ],
        ];
    }
}
