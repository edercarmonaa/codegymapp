<?php

declare(strict_types=1);

use CodeGymApp\Services\CalendarService;
use CodeGymApp\Services\DashboardService;

final class ApiMobileController
{
    public function __construct(
        private readonly CalendarService $calendarService = new CalendarService(),
        private readonly DashboardService $dashboardService = new DashboardService()
    ) {
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

    public function planned(): void
    {
        Challenge::expirePending();

        Response::json([
            'ok' => true,
            'planned' => array_map([$this, 'challengeResource'], Challenge::mobilePlanned()),
        ]);
    }

    public function challenges(): void
    {
        Challenge::expirePending();

        $filters = $this->challengeFilters($_GET);
        Response::json([
            'ok' => true,
            'filters' => $filters,
            'challenges' => array_map([$this, 'challengeResource'], Challenge::mobileChallenges($filters)),
        ]);
    }

    public function summary(): void
    {
        $this->dashboardService->refreshDashboardData();
        $payload = $this->dashboardService->dashboardPayload();
        $stats = $payload['stats'] ?? [];
        $streaks = $payload['streaks'] ?? [];
        $attention = $payload['attention'] ?? [];

        Response::json([
            'ok' => true,
            'summary' => [
                'completed_month' => (int) ($stats['completed_month'] ?? 0),
                'general_percent' => (float) ($stats['general_percent'] ?? 0),
                'on_time_percent' => (float) ($stats['on_time_percent'] ?? 0),
                'time_month' => (int) ($stats['time_month'] ?? 0),
                'current_streak' => (int) ($streaks['current'] ?? 0),
                'best_streak' => (int) ($streaks['best'] ?? 0),
                'month_streak' => (int) ($streaks['month'] ?? 0),
                'expired_review' => (int) ($stats['expired_review'] ?? 0),
                'pending_today' => (int) ($attention['pending_today'] ?? 0),
                'pending_week' => (int) ($attention['pending_week'] ?? 0),
            ],
        ]);
    }

    public function createOptions(): void
    {
        Response::json([
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], Platform::active()),
        ]);
    }

    public function storeChallenge(): void
    {
        $this->respond($this->calendarService->createChallenge($this->jsonInput()));
    }

    public function saveChallengeDetails(): void
    {
        $this->respond($this->calendarService->saveChallengeDetails($this->jsonInput()));
    }

    public function completeChallenge(): void
    {
        $this->respond($this->calendarService->completeChallenge($this->jsonInput()));
    }

    public function missChallenge(): void
    {
        $this->respond($this->calendarService->missChallenge($this->jsonInput()));
    }

    public function storeRoutine(): void
    {
        $this->respond($this->calendarService->createRoutine($this->jsonInput()));
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

    /** @param array<string, mixed> $platform @return array<string, mixed> */
    private function platformResource(array $platform): array
    {
        return [
            'id' => (int) ($platform['id'] ?? 0),
            'name' => (string) ($platform['name'] ?? ''),
        ];
    }

    /**
     * @param array<string, mixed> $input
     * @return array{month: string, status: string}
     */
    private function challengeFilters(array $input): array
    {
        $month = (string) ($input['month'] ?? date('Y-m'));
        $status = (string) ($input['status'] ?? 'pending');

        return [
            'month' => preg_match('/^\d{4}-\d{2}$/', $month) === 1 ? $month : date('Y-m'),
            'status' => in_array($status, ['pending', 'completed', 'expired', 'missed', 'cancelled', 'all'], true) ? $status : 'pending',
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
