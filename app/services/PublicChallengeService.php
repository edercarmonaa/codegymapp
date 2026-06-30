<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class PublicChallengeService
{
    /**
     * @param array<string, mixed> $query
     * @return array<string, mixed>
     */
    public function indexPayload(array $query): array
    {
        $filters = $this->filtersFromQuery($query);
        $state = \TableState::fromRequest(
            ['completed_date', 'platform', 'title', 'difficulty', 'languages', 'challenge_url', 'github'],
            'completed_date',
            'desc'
        );

        return [
            'title' => 'Retos cumplidos',
            'challenges' => \Challenge::publicCompletedList($filters, $state),
            'pagination' => \TableState::pagination($state, \Challenge::countPublicCompleted($filters)),
            'platforms' => \Platform::active(),
            'languages' => \Language::active(),
            'difficultyOptions' => [
                'facil' => 'Fácil',
                'medio' => 'Medio',
                'dificil' => 'Difícil',
            ],
            'filters' => $filters,
            'sort' => (string) $state['sort'],
            'dir' => (string) $state['dir'],
        ];
    }

    /**
     * @param array<string, mixed> $query
     * @return array{platform_id: int, language_id: int, difficulty: string}
     */
    private function filtersFromQuery(array $query): array
    {
        $difficulty = strtolower(substr((string) ($query['difficulty'] ?? ''), 0, 20));
        if (!in_array($difficulty, ['facil', 'medio', 'dificil'], true)) {
            $difficulty = '';
        }

        return [
            'platform_id' => max(0, (int) ($query['platform_id'] ?? 0)),
            'language_id' => max(0, (int) ($query['language_id'] ?? 0)),
            'difficulty' => $difficulty,
        ];
    }
}
