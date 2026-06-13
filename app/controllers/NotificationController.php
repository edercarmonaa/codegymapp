<?php

declare(strict_types=1);

final class NotificationController
{
    public function index(): void
    {
        View::render('notifications/index', ['title' => 'Notificaciones'], 'main');
    }
}

