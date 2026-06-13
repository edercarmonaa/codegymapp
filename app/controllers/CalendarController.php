<?php

declare(strict_types=1);

final class CalendarController
{
    public function index(): void
    {
        Routine::generateCurrentMonth();
        Challenge::expirePending();
        View::render('calendar/index', [
            'title' => 'Calendario',
            'platforms' => Platform::all(),
            'languages' => Language::all(),
            'routines' => Routine::allForList(),
        ], 'main');
    }
}
