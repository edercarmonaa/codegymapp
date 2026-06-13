<?php

declare(strict_types=1);

final class NotificationController
{
    public function index(): void
    {
        Challenge::expirePending();
        Notification::generateSystemNotifications();
        $state = TableState::fromRequest(['title', 'is_read', 'created_at'], 'created_at', 'desc');

        View::render('notifications/index', [
            'title' => 'Notificaciones',
            'notifications' => Notification::paginated($state),
            'pagination' => TableState::pagination($state, Notification::countAll()),
        ], 'main');
    }

    public function markRead(): void
    {
        verify_csrf();
        Notification::markRead((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Notificación marcada como leída.';
        Response::redirect('/notificaciones');
    }

    public function delete(): void
    {
        verify_csrf();
        Notification::delete((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Notificación eliminada.';
        Response::redirect('/notificaciones');
    }
}
