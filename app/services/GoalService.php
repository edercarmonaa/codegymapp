<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class GoalService
{
    public function __construct(private readonly GoalRequestValidator $goalValidator = new GoalRequestValidator())
    {
    }

    /** @return array<string, mixed> */
    public function indexPayload(): array
    {
        \Goal::refreshActiveProgress();
        $state = \TableState::fromRequest(['goal_type', 'period_end', 'progress_percent', 'status'], 'period_end', 'asc');

        return [
            'title' => 'Metas',
            'goals' => \Goal::paginated($state),
            'pagination' => \TableState::pagination($state, \Goal::countAll()),
            'platforms' => \Platform::active(),
            'languages' => \Language::active(),
            'goalTypes' => \Goal::goalTypes(),
            'periodTypes' => \Goal::periodTypes(),
        ];
    }

    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string}
     */
    public function create(array $input): array
    {
        $validated = $this->goalValidator->validate($input);
        if (!$validated['ok']) {
            return ['ok' => false, 'message' => (string) $validated['message']];
        }

        \Goal::create($validated['data']);
        return ['ok' => true];
    }

    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string}
     */
    public function update(array $input): array
    {
        $id = (int) ($input['id'] ?? 0);
        $validated = $this->goalValidator->validate($input);
        if ($id <= 0) {
            return ['ok' => false, 'message' => 'No se pudo identificar la meta.'];
        }

        if (!$validated['ok']) {
            return ['ok' => false, 'message' => (string) $validated['message']];
        }

        if (!\Goal::updateActive($id, $validated['data'])) {
            return ['ok' => false, 'message' => 'Esta meta ya no se puede editar.'];
        }

        \Goal::refreshActiveProgress();
        return ['ok' => true];
    }

    public function deactivate(int $id): void
    {
        \Goal::close($id);
    }
}
