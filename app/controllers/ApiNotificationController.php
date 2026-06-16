<?php

declare(strict_types=1);

use CodeGymApp\Services\NotificationService;

final class ApiNotificationController
{
    public function __construct(private readonly NotificationService $notificationService = new NotificationService())
    {
    }

    public function list(): void
    {
        $data = $this->notificationService->indexPayload();
        Response::json([
            'ok' => true,
            'notifications' => array_map([$this, 'notificationResource'], $data['notifications']),
            'pagination' => $data['pagination'],
        ]);
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

    /** @param array<string, mixed> $notification @return array<string, mixed> */
    private function notificationResource(array $notification): array
    {
        return [
            'id' => (int) ($notification['id'] ?? 0),
            'title' => (string) ($notification['title'] ?? ''),
            'message' => (string) ($notification['message'] ?? ''),
            'is_read' => (int) ($notification['is_read'] ?? 0) === 1,
            'action_url' => safe_app_url($notification['action_url'] ?? '', ''),
            'created_at' => (string) ($notification['created_at'] ?? ''),
            'read_at' => $notification['read_at'] ?? null,
        ];
    }
}
