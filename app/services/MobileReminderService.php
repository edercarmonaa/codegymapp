<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class MobileReminderService
{
    public function __construct(
        private readonly AzureNotificationHubService $notificationHubService = new AzureNotificationHubService()
    ) {
    }

    /** @return array{ok: bool, sent: int, skipped: bool, forced: bool, pending: int, users: int, recipients: list<array{user_id: int, azure_accepted: bool, detail: string|null}>, message: string} */
    public function sendTodayReminder(bool $force = false): array
    {
        \Challenge::expirePending();

        $pending = count(\Challenge::todayPending());
        if ($pending <= 0) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => false,
                'forced' => $force,
                'pending' => 0,
                'users' => 0,
                'recipients' => [],
                'message' => 'No hay retos pendientes para hoy.',
            ];
        }

        $notificationType = 'mobile_today_reminder';
        $actionUrl = '/calendario';
        $userIds = \MobileDeviceToken::activeUserIds();
        if (!$force && \Notification::existsToday($notificationType, $actionUrl)) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => true,
                'forced' => false,
                'pending' => $pending,
                'users' => count($userIds),
                'recipients' => [],
                'message' => 'El recordatorio de hoy ya fue enviado. Usa force=1 para reenviarlo manualmente.',
            ];
        }

        $sent = 0;
        $recipients = [];
        foreach ($userIds as $userId) {
            $accepted = $this->notificationHubService->sendToUser($userId, 'Retos pendientes', $this->message($pending), [
                'type' => 'today_reminder',
                'screen' => 'today',
                'action_url' => $actionUrl,
            ]);
            $recipients[] = [
                'user_id' => $userId,
                'azure_accepted' => $accepted,
                'detail' => $accepted ? null : $this->notificationHubService->lastError(),
            ];

            if ($accepted) {
                $sent++;
            }
        }

        $result = [
            'ok' => $sent > 0,
            'sent' => $sent,
            'skipped' => false,
            'forced' => $force,
            'pending' => $pending,
            'users' => count($userIds),
            'recipients' => $recipients,
            'message' => $sent > 0
                ? ($force ? 'Recordatorio reenviado manualmente.' : 'Recordatorio enviado.')
                : ($this->notificationHubService->lastError() ?? 'No se pudo enviar el recordatorio.'),
        ];

        if ($sent > 0) {
            \Notification::createOnceToday($notificationType, 'Retos pendientes', $this->message($pending), $actionUrl);
        }

        return $result;
    }

    /** @return array{ok: bool, sent: int, skipped: bool, forced: bool, pending: int, users: int, recipients: list<array{user_id: int, azure_accepted: bool, detail: string|null}>, message: string} */
    public function sendExpiredReviewReminder(bool $force = false): array
    {
        \Challenge::expirePending();

        $pending = count(\Challenge::expiredForReview());
        if ($pending <= 0) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => false,
                'forced' => $force,
                'pending' => 0,
                'users' => 0,
                'recipients' => [],
                'message' => 'No hay retos vencidos pendientes de revisar.',
            ];
        }

        $notificationType = 'mobile_expired_review_reminder';
        $actionUrl = '/retos?status=expired';
        $userIds = \MobileDeviceToken::activeUserIds();
        if (!$force && \Notification::existsToday($notificationType, $actionUrl)) {
            return [
                'ok' => true,
                'sent' => 0,
                'skipped' => true,
                'forced' => false,
                'pending' => $pending,
                'users' => count($userIds),
                'recipients' => [],
                'message' => 'El recordatorio de retos vencidos ya fue enviado. Usa force=1 para reenviarlo manualmente.',
            ];
        }

        $sent = 0;
        $recipients = [];
        foreach ($userIds as $userId) {
            $accepted = $this->notificationHubService->sendToUser($userId, 'Retos vencidos', $this->expiredReviewMessage($pending), [
                'type' => 'expired_review_reminder',
                'screen' => 'challenges_expired',
                'action_url' => $actionUrl,
            ]);
            $recipients[] = [
                'user_id' => $userId,
                'azure_accepted' => $accepted,
                'detail' => $accepted ? null : $this->notificationHubService->lastError(),
            ];

            if ($accepted) {
                $sent++;
            }
        }

        $result = [
            'ok' => $sent > 0,
            'sent' => $sent,
            'skipped' => false,
            'forced' => $force,
            'pending' => $pending,
            'users' => count($userIds),
            'recipients' => $recipients,
            'message' => $sent > 0
                ? ($force ? 'Recordatorio de retos vencidos reenviado manualmente.' : 'Recordatorio de retos vencidos enviado.')
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
