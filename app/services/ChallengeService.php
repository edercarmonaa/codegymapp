<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class ChallengeService
{
    public function __construct(
        private readonly ManualChallengeRequestValidator $manualChallengeValidator = new ManualChallengeRequestValidator()
    ) {
    }

    /**
     * @param array<string, mixed> $query
     * @return array<string, mixed>
     */
    public function indexPayload(array $query): array
    {
        \Challenge::expirePending();
        $filters = $this->filtersFromQuery($query);
        $state = \TableState::fromRequest(
            ['scheduled_date', 'platform', 'status', 'completed_date', 'time_spent_minutes'],
            'scheduled_date',
            'desc'
        );

        return [
            'title' => 'Retos',
            'challenges' => \Challenge::allForList($filters, $state),
            'pagination' => \TableState::pagination($state, \Challenge::countForList($filters)),
            'platforms' => \Platform::all(),
            'activePlatforms' => \Platform::active(),
            'activeLanguages' => \Language::active(),
            'statusLabels' => \Challenge::statusLabels(),
            'statusBadgeClasses' => \Challenge::statusBadgeClasses(),
            'filters' => $filters,
        ];
    }

    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, errors: array<int, string>}
     */
    public function createManual(array $input): array
    {
        $validated = $this->manualChallengeValidator->validate($input);
        if (!$validated['ok']) {
            return ['ok' => false, 'errors' => $validated['errors']];
        }

        \Challenge::createManual($validated['data']);
        return ['ok' => true, 'errors' => []];
    }

    /** @param array<string, mixed> $query @return array{status: string, platform_id: int} */
    private function filtersFromQuery(array $query): array
    {
        return [
            'status' => substr((string) ($query['status'] ?? ''), 0, 30),
            'platform_id' => max(0, (int) ($query['platform_id'] ?? 0)),
        ];
    }
}
