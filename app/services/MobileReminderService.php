<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class MobileReminderService
{
    public function __construct(
        private readonly AzureNotificationHubService $notificationHubService = new AzureNotificationHubService()
    ) {
    }

    /** @return array{ok: bool, sent: int, skipped: bool, pending: int, users: int, message: string} */
    public function sendTodayReminder(): array
    {
        \Challenge::expirePending();

        $pending = count(\Challenge::todayPending());
        if ($pending <= 0) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => false,
                'pending' => 0,
                'users' => 0,
                'message' => 'No hay retos pendientes para hoy.',
            ];
        }

        $notificationType = 'mobile_today_reminder';
        $actionUrl = '/calendario';
        if (\Notification::existsToday($notificationType, $actionUrl)) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => true,
                'pending' => $pending,
                'users' => 0,
                'message' => 'El recordatorio de hoy ya fue enviado.',
            ];
        }

        $sent = 0;
        $userIds = \MobileDeviceToken::activeUserIds();
        foreach ($userIds as $userId) {
            if ($this->notificationHubService->sendToUser($userId, 'Retos pendientes', $this->message($pending), [
                'type' => 'today_reminder',
                'screen' => 'today',
                'action_url' => $actionUrl,
            ])) {
                $sent++;
            }
        }

        $result = [
            'ok' => $sent > 0,
            'sent' => $sent,
            'skipped' => false,
            'pending' => $pending,
            'users' => count($userIds),
            'message' => $sent > 0
                ? 'Recordatorio enviado.'
                : ($this->notificationHubService->lastError() ?? 'No se pudo enviar el recordatorio.'),
        ];

        if ($sent > 0) {
            \Notification::createOnceToday($notificationType, 'Retos pendientes', $this->message($pending), $actionUrl);
        }

        return $result;
    }

    /** @return array{ok: bool, sent: int, skipped: bool, pending: int, users: int, message: string} */
    public function sendExpiredReviewReminder(): array
    {
        \Challenge::expirePending();

        $pending = count(\Challenge::expiredForReview());
        if ($pending <= 0) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => false,
                'pending' => 0,
                'users' => 0,
                'message' => 'No hay retos vencidos pendientes de revisar.',
            ];
        }

        $notificationType = 'mobile_expired_review_reminder';
        $actionUrl = '/retos?status=expired';
        if (\Notification::existsToday($notificationType, $actionUrl)) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => true,
                'pending' => $pending,
                'users' => 0,
                'message' => 'El recordatorio de retos vencidos ya fue enviado.',
            ];
        }

        $sent = 0;
        $userIds = \MobileDeviceToken::activeUserIds();
        foreach ($userIds as $userId) {
            if ($this->notificationHubService->sendToUser($userId, 'Retos vencidos', $this->expiredReviewMessage($pending), [
                'type' => 'expired_review_reminder',
                'screen' => 'challenges_expired',
                'action_url' => $actionUrl,
            ])) {
                $sent++;
            }
        }

        $result = [
            'ok' => $sent > 0,
            'sent' => $sent,
            'skipped' => false,
            'pending' => $pending,
            'users' => count($userIds),
            'message' => $sent > 0
                ? 'Recordatorio de retos vencidos enviado.'
                : ($this->notificationHubService->lastError() ?? 'No se pudo enviar el recordatorio de retos vencidos.'),
        ];

        if ($sent > 0) {
            \Notification::createOnceToday($notificationType, 'Retos vencidos', $this->expiredReviewMessage($pending), $actionUrl);
        }

        return $result;
    }

    private function message(int $pending): string
    {
        return 'Tienes ' . $pending . ' reto' . ($pending === 1 ? '' : 's') . ' pendiente' . ($pending === 1 ? '' : 's') . ' para hoy.';
    }

    private function expiredReviewMessage(int $pending): string
    {
        return 'Tienes ' . $pending . ' reto' . ($pending === 1 ? '' : 's') . ' vencido' . ($pending === 1 ? '' : 's') . ' pendiente' . ($pending === 1 ? '' : 's') . ' de revisar.';
    }
}
