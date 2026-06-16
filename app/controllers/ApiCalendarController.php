<?php

declare(strict_types=1);

use CodeGymApp\Services\CalendarService;

final class ApiCalendarController
{
    public function __construct(private readonly CalendarService $calendarService = new CalendarService())
    {
    }

    public function events(): void
    {
        Response::json($this->calendarService->events($_GET));
    }

    public function challenge(): void
    {
        $this->respond($this->calendarService->challengeDetail((int) ($_GET['id'] ?? 0)));
    }

    public function store(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->createChallenge($_POST));
    }

    public function updateDate(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->updateChallengeDate($this->jsonInput()));
    }

    public function saveDetails(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->saveChallengeDetails($_POST));
    }

    public function complete(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->completeChallenge($_POST));
    }

    public function miss(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->missChallenge($_POST));
    }

    public function cancel(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->cancelChallenge($_POST));
    }

    public function storeRoutine(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->createRoutine($_POST));
    }

    public function updateRoutine(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->updateRoutine($_POST));
    }

    public function disableRoutine(): void
    {
        verify_csrf();
        $this->respond($this->calendarService->disableRoutine($_POST));
    }

    /** @return array<string, mixed> */
    private function jsonInput(): array
    {
        $payload = json_decode(file_get_contents('php://input') ?: '{}', true);
        return is_array($payload) ? $payload : [];
    }

    /** @param array{status: int, payload: array<string, mixed>} $response */
    private function respond(array $response): void
    {
        if ($response['status'] !== 200) {
            http_response_code($response['status']);
        }

        Response::json($response['payload']);
    }
}
