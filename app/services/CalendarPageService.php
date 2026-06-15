<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class CalendarPageService
{
    public function __construct(private readonly CalendarService $calendarService = new CalendarService())
    {
    }

    /** @return array<string, mixed> */
    public function pagePayload(): array
    {
        $this->calendarService->refreshCalendar();

        return [
            'title' => 'Calendario',
            'platforms' => \Platform::all(),
            'languages' => \Language::all(),
            'routines' => \Routine::allForList(),
        ];
    }
}
