<?php

declare(strict_types=1);

use CodeGymApp\Services\NotificationService;

final class NotificationController
{
    public function __construct(private readonly NotificationService $notificationService = new NotificationService())
    {
    }

    public function index(): void
    {
        $data = $this->notificationService->indexPayload();
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('notifications/table', $data);
            return;
        }
        View::render('notifications/index', $data, 'main');
    }

    public function markRead(): void
    {
        verify_csrf();
        $this->notificationService->markRead((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Notificación marcada como leída.';
        Response::redirect('/notificaciones');
    }

    public function delete(): void
    {
        verify_csrf();
        $this->notificationService->delete((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Notificación eliminada.';
        Response::redirect('/notificaciones');
    }
}
