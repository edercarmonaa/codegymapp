<?php

declare(strict_types=1);

use CodeGymApp\Services\ChallengeService;

final class ApiChallengeController
{
    public function __construct(private readonly ChallengeService $challengeService = new ChallengeService())
    {
    }

    public function list(): void
    {
        $data = $this->challengeService->indexPayload($_GET);
        Response::json([
            'ok' => true,
            'challenges' => array_map([$this, 'challengeResource'], $data['challenges']),
            'pagination' => $data['pagination'],
            'platforms' => array_map([$this, 'platformResource'], $data['platforms']),
            'statusLabels' => $data['statusLabels'],
            'statusBadgeClasses' => $data['statusBadgeClasses'],
            'filters' => $data['filters'],
        ]);
    }

    public function manual(): void
    {
        verify_csrf();
        $result = $this->challengeService->createManual($_POST);
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => implode(' ', $result['errors']),
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto manual registrado correctamente.']);
    }

    /** @param array<string, mixed> $challenge @return array<string, mixed> */
    private function challengeResource(array $challenge): array
    {
        $githubUrls = array_values(array_filter(array_map('safe_url', explode("\n", (string) ($challenge['github_urls'] ?? '')))));

        return [
            'id' => (int) ($challenge['id'] ?? 0),
            'scheduled_date' => (string) ($challenge['scheduled_date'] ?? ''),
            'completed_date' => $challenge['completed_date'] ?? null,
            'platform_name' => (string) ($challenge['platform_name'] ?? ''),
            'title' => (string) ($challenge['title'] ?? ''),
            'challenge_url' => safe_url($challenge['challenge_url'] ?? null),
            'status' => (string) ($challenge['status'] ?? ''),
            'difficulty' => (string) ($challenge['difficulty'] ?? ''),
            'language_names' => (string) ($challenge['language_names'] ?? ''),
            'time_spent_minutes' => (int) ($challenge['time_spent_minutes'] ?? 0),
            'github_urls' => $githubUrls,
            'is_rescheduled' => (int) ($challenge['is_rescheduled'] ?? 0) === 1,
            'origin' => (string) ($challenge['origin'] ?? ''),
        ];
    }

    /** @param array<string, mixed> $platform @return array<string, mixed> */
    private function platformResource(array $platform): array
    {
        return [
            'id' => (int) ($platform['id'] ?? 0),
            'name' => (string) ($platform['name'] ?? ''),
            'is_active' => (int) ($platform['is_active'] ?? 0) === 1,
        ];
    }
}
