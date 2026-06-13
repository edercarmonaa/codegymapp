<?php

declare(strict_types=1);

final class SecurityLogController
{
    public function index(): void
    {
        $state = TableState::fromRequest(['created_at', 'event_type', 'result', 'ip_address'], 'created_at', 'desc');
        $data = [
            'title' => 'Seguridad',
            'logs' => SecurityLog::paginate((int) $state['per_page'], (int) $state['offset'], (string) $state['sort'], (string) $state['dir']),
            'pagination' => TableState::pagination($state, SecurityLog::countAll()),
        ];
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('security/table', $data);
            return;
        }
        View::render('security/index', $data, 'main');
    }
}
