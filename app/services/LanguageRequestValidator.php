<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class LanguageRequestValidator
{
    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string, data?: array<string, string>}
     */
    public function validate(array $input): array
    {
        $name = trim((string) ($input['name'] ?? ''));
        if ($name === '') {
            return ['ok' => false, 'message' => 'Captura el nombre del lenguaje.'];
        }

        return [
            'ok' => true,
            'data' => [
                'id' => (string) ($input['id'] ?? ''),
                'name' => $name,
            ],
        ];
    }
}
