<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class SecurityLogService
{
    /** @return array<string, mixed> */
    public function indexPayload(): array
    {
        $state = \TableState::fromRequest(['created_at', 'event_type', 'result', 'ip_address'], 'created_at', 'desc');

        return [
            'title' => 'Seguridad',
            'logs' => \SecurityLog::paginate(
                (int) $state['per_page'],
                (int) $state['offset'],
                (string) $state['sort'],
                (string) $state['dir']
            ),
            'pagination' => \TableState::pagination($state, \SecurityLog::countAll()),
        ];
    }
}
