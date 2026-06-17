<?php

declare(strict_types=1);

final class ApiMobileController
{
    public function today(): void
    {
        Challenge::expirePending();

        Response::json([
            'ok' => true,
            'today' => array_map([$this, 'challengeResource'], Challenge::todayPending()),
            'expired' => array_map([$this, 'challengeResource'], Challenge::expiredForReview()),
        ]);
    }

    /** @param array<string, mixed> $challenge @return array<string, mixed> */
    private function challengeResource(array $challenge): array
    {
        return [
            'id' => (int) ($challenge['id'] ?? 0),
            'platform_name' => (string) ($challenge['platform_name'] ?? ''),
            'title' => (string) ($challenge['title'] ?? ''),
            'scheduled_date' => (string) ($challenge['scheduled_date'] ?? ''),
            'completed_date' => $challenge['completed_date'] ?? null,
            'status' => (string) ($challenge['status'] ?? ''),
            'difficulty' => (string) ($challenge['difficulty'] ?? ''),
            'challenge_url' => safe_url($challenge['challenge_url'] ?? null),
            'time_spent_minutes' => (int) ($challenge['time_spent_minutes'] ?? 0),
            'notes' => (string) ($challenge['notes'] ?? ''),
            'origin' => (string) ($challenge['origin'] ?? ''),
            'is_rescheduled' => (int) ($challenge['is_rescheduled'] ?? 0) === 1,
        ];
    }
}
