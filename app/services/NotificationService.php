<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class NotificationService
{
    /** @return array<string, mixed> */
    public function indexPayload(): array
    {
        \Challenge::expirePending();
        \Notification::generateSystemNotifications();
        $state = \TableState::fromRequest(['title', 'is_read', 'created_at'], 'created_at', 'desc');

        return [
            'title' => 'Notificaciones',
            'notifications' => \Notification::paginated($state),
            'pagination' => \TableState::pagination($state, \Notification::countAll()),
        ];
    }

    public function markRead(int $id): void
    {
        \Notification::markRead($id);
    }

    public function delete(int $id): void
    {
        \Notification::delete($id);
    }
}
