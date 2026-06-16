<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class ManualChallengeRequestValidator
{
    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, errors: array<int, string>, data: array<string, mixed>}
     */
    public function validate(array $input): array
    {
        $title = trim((string) ($input['title'] ?? ''));
        $difficulty = trim((string) ($input['difficulty'] ?? ''));
        $timeSpent = (int) ($input['time_spent_minutes'] ?? 0);
        $platformId = (int) ($input['platform_id'] ?? 0);
        $languageIds = $input['language_ids'] ?? [];
        $errors = [];

        if ($platformId <= 0 || !\Platform::existsActive($platformId)) {
            $errors[] = 'Selecciona una plataforma activa.';
        }
        if ($title === '') {
            $errors[] = 'Captura el nombre del reto.';
        }
        if ($difficulty === '') {
            $errors[] = 'Captura la dificultad.';
        }
        if ($timeSpent <= 0) {
            $errors[] = 'Captura el tiempo invertido.';
        }
        if (!is_array($languageIds) || count(array_filter($languageIds)) === 0) {
            $errors[] = 'Selecciona al menos un lenguaje.';
        }

        return [
            'ok' => $errors === [],
            'errors' => $errors,
            'data' => [
                'platform_id' => $platformId,
                'title' => $title,
                'challenge_url' => (string) ($input['challenge_url'] ?? ''),
                'difficulty' => $difficulty,
                'time_spent_minutes' => $timeSpent,
                'notes' => (string) ($input['notes'] ?? ''),
                'language_ids' => is_array($languageIds) ? $languageIds : [],
                'github_links' => (string) ($input['github_links'] ?? ''),
            ],
        ];
    }
}
