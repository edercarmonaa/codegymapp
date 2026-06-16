<?php

declare(strict_types=1);

use CodeGymApp\Services\NotificationService;

final class ApiNotificationController
{
    public function __construct(private readonly NotificationService $notificationService = new NotificationService())
    {
    }

    public function markRead(): void
    {
        verify_csrf();
        $this->notificationService->markRead((int) ($_POST['id'] ?? 0));
        Response::json(['ok' => true, 'message' => 'Notificación marcada como leída.']);
    }

    public function delete(): void
    {
        verify_csrf();
        $this->notificationService->delete((int) ($_POST['id'] ?? 0));
        Response::json(['ok' => true, 'message' => 'Notificación eliminada.']);
    }
}
