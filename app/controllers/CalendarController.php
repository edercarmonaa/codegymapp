<?php

declare(strict_types=1);

use CodeGymApp\Services\CalendarPageService;

final class CalendarController
{
    public function __construct(private readonly CalendarPageService $calendarPageService = new CalendarPageService())
    {
    }

    public function index(): void
    {
        View::render('calendar/index', $this->calendarPageService->pagePayload(), 'main');
    }
}
