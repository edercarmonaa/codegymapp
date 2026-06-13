<?php

declare(strict_types=1);

final class SecurityLogController
{
    public function index(): void
    {
        View::render('security/index', ['title' => 'Seguridad', 'logs' => SecurityLog::paginate()], 'main');
    }
}

