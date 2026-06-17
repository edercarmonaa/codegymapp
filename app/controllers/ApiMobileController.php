<?php

declare(strict_types=1);

use CodeGymApp\Services\CalendarService;

final class ApiMobileController
{
    public function __construct(private readonly CalendarService $calendarService = new CalendarService())
    {
    }

    public function today(): void
    {
        Challenge::expirePending();

        Response::json([
            'ok' => true,
            'today' => array_map([$this, 'challengeResource'], Challenge::todayPending()),
            'expired' => array_map([$this, 'challengeResource'], Challenge::expiredForReview()),
        ]);
    }

    public function completeChallenge(): void
    {
        $this->respond($this->calendarService->completeChallenge($this->jsonInput()));
    }

    public function missChallenge(): void
    {
        $this->respond($this->calendarService->missChallenge($this->jsonInput()));
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

    /** @return array<string, mixed> */
    private function jsonInput(): array
    {
        $payload = json_decode(file_get_contents('php://input') ?: '{}', true);
        return is_array($payload) ? $payload : [];
    }

    /** @param array{status: int, payload: array<string, mixed>} $response */
    private function respond(array $response): void
    {
        if ($response['status'] !== 200) {
            http_response_code($response['status']);
        }

        Response::json($response['payload']);
    }
}
