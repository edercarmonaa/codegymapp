<?php

declare(strict_types=1);

final class CalendarController
{
    public function index(): void
    {
        Challenge::expirePending();
        View::render('calendar/index', [
            'title' => 'Calendario',
            'platforms' => Platform::active(),
        ], 'main');
    }
}
