<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class PlatformService
{
    public function __construct(private readonly PlatformRequestValidator $validator = new PlatformRequestValidator())
    {
    }

    /** @return array<string, mixed> */
    public function indexPayload(): array
    {
        $state = \TableState::fromRequest(['name', 'is_active', 'created_at'], 'name', 'asc');

        return [
            'title' => 'Plataformas',
            'platforms' => \Platform::paginated($state),
            'pagination' => \TableState::pagination($state, \Platform::countAll()),
        ];
    }

    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string}
     */
    public function save(array $input): array
    {
        $validated = $this->validator->validate($input);
        if (!$validated['ok']) {
            return ['ok' => false, 'message' => (string) $validated['message']];
        }

        \Platform::save($validated['data']);
        return ['ok' => true];
    }

    public function setActive(int $id, bool $active): void
    {
        \Platform::setActive($id, $active);
    }
}
