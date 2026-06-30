<?php

declare(strict_types=1);

use CodeGymApp\Services\CalendarService;
use CodeGymApp\Services\AzureNotificationHubService;
use CodeGymApp\Services\ChallengeService;
use CodeGymApp\Services\DashboardService;
use CodeGymApp\Services\GoalService;

final class ApiMobileController
{
    public function __construct(
        private readonly CalendarService $calendarService = new CalendarService(),
        private readonly ChallengeService $challengeService = new ChallengeService(),
        private readonly DashboardService $dashboardService = new DashboardService(),
        private readonly GoalService $goalService = new GoalService(),
        private readonly AzureNotificationHubService $notificationHubService = new AzureNotificationHubService()
    ) {
    }

    public function today(): void
    {
        Challenge::expirePending();

        Response::json([
            'ok' => true,
            'today' => array_map([$this, 'challengeResource'], Challenge::todayPending()),
            'expired' => array_map([$this, 'challengeResource'], Challenge::expiredForReview()),
        ]);
    }

    public function planned(): void
    {
        Challenge::expirePending();

        Response::json([
            'ok' => true,
            'planned' => array_map([$this, 'challengeResource'], Challenge::mobilePlanned()),
        ]);
    }

    public function challenges(): void
    {
        Challenge::expirePending();

        $filters = $this->challengeFilters($_GET);
        Response::json([
            'ok' => true,
            'filters' => $filters,
            'challenges' => array_map([$this, 'challengeResource'], Challenge::mobileChallenges($filters)),
        ]);
    }

    public function summary(): void
    {
        $this->dashboardService->refreshDashboardData();
        $payload = $this->dashboardService->dashboardPayload();
        $stats = $payload['stats'] ?? [];
        $streaks = $payload['streaks'] ?? [];
        $attention = $payload['attention'] ?? [];
        $distribution = $payload['distribution'] ?? [];

        Response::json([
            'ok' => true,
            'summary' => [
                'completed_month' => (int) ($stats['completed_month'] ?? 0),
                'general_percent' => (float) ($stats['general_percent'] ?? 0),
                'on_time_percent' => (float) ($stats['on_time_percent'] ?? 0),
                'time_month' => (int) ($stats['time_month'] ?? 0),
                'current_streak' => (int) ($streaks['current'] ?? 0),
                'best_streak' => (int) ($streaks['best'] ?? 0),
                'month_streak' => (int) ($streaks['month'] ?? 0),
                'expired_review' => (int) ($stats['expired_review'] ?? 0),
                'pending_today' => (int) ($attention['pending_today'] ?? 0),
                'pending_week' => (int) ($attention['pending_week'] ?? 0),
                'days_without_practice' => (int) ($attention['days_without_practice'] ?? 0),
                'distribution' => [
                    'pending' => (int) ($distribution['pending'] ?? 0),
                    'completed' => (int) ($distribution['completed'] ?? 0),
                    'missed' => (int) ($distribution['missed'] ?? 0),
                    'expired' => (int) ($distribution['expired'] ?? 0),
                    'cancelled' => (int) ($distribution['cancelled'] ?? 0),
                ],
                'weekly_compliance' => array_map([$this, 'summarySeriesResource'], $payload['weeklyCompliance'] ?? []),
                'top_platforms' => array_map([$this, 'summarySeriesResource'], $payload['topPlatforms'] ?? []),
                'top_languages' => array_map([$this, 'summarySeriesResource'], $payload['topLanguages'] ?? []),
            ],
        ]);
    }

    public function notifications(): void
    {
        $this->dashboardService->refreshDashboardData();

        Response::json([
            'ok' => true,
            'unread_count' => Notification::unreadCount(),
            'notifications' => array_map([$this, 'notificationResource'], Notification::allForList()),
        ]);
    }

    public function markNotificationRead(): void
    {
        $id = (int) ($this->jsonInput()['id'] ?? 0);
        if ($id <= 0) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo identificar la notificación.']);
            return;
        }

        Notification::markRead($id);
        Response::json(['ok' => true, 'message' => 'Notificación marcada como leída.']);
    }

    public function deleteNotification(): void
    {
        $id = (int) ($this->jsonInput()['id'] ?? 0);
        if ($id <= 0) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo identificar la notificación.']);
            return;
        }

        Notification::delete($id);
        Response::json(['ok' => true, 'message' => 'Notificación eliminada.']);
    }

    public function storeDeviceToken(): void
    {
        $user = Auth::user();
        $input = $this->jsonInput();
        if (!$user || !MobileDeviceToken::upsert((int) $user['id'], $input)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo registrar el dispositivo.']);
            return;
        }

        $azureRegistered = $this->notificationHubService->registerInstallation($user, $input);

        Response::json([
            'ok' => true,
            'message' => $azureRegistered
                ? 'Dispositivo registrado.'
                : 'Dispositivo guardado, pero Azure no confirmó el registro push.',
            'push' => [
                'local_saved' => true,
                'azure_registered' => $azureRegistered,
                'detail' => $azureRegistered ? null : $this->notificationHubService->lastError(),
            ],
        ]);
    }

    public function testNotification(): void
    {
        $user = Auth::user();
        if (!$user) {
            http_response_code(401);
            Response::json(['ok' => false, 'message' => 'Sesión no válida.']);
            return;
        }

        $input = $this->jsonInput();
        $title = trim((string) ($input['title'] ?? 'CodeGymApp'));
        $message = trim((string) ($input['message'] ?? 'Notificación de prueba enviada desde CodeGymApp.'));

        $sent = $this->notificationHubService->sendToUser(
            (int) $user['id'],
            $title !== '' ? $title : 'CodeGymApp',
            $message !== '' ? $message : 'Notificación de prueba enviada desde CodeGymApp.',
            [
                'type' => 'test',
                'screen' => 'notifications',
                'action_url' => '/notificaciones',
            ]
        );

        if (!$sent) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => 'No se pudo enviar la notificación de prueba.',
                'detail' => $this->notificationHubService->lastError(),
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Notificación de prueba enviada.']);
    }

    public function createOptions(): void
    {
        Response::json([
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], Platform::active()),
            'languages' => array_map([$this, 'languageResource'], Language::active()),
        ]);
    }

    public function storeChallenge(): void
    {
        $this->respond($this->calendarService->createChallenge($this->jsonInput()));
    }

    public function manualChallenge(): void
    {
        $result = $this->challengeService->createManual($this->jsonInput());
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => implode(' ', $result['errors']),
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto realizado registrado correctamente.']);
    }

    public function saveChallengeDetails(): void
    {
        $this->respond($this->calendarService->saveChallengeDetails($this->jsonInput()));
    }

    public function completeChallenge(): void
    {
        $this->respond($this->calendarService->completeChallenge($this->jsonInput()));
    }

    public function missChallenge(): void
    {
        $this->respond($this->calendarService->missChallenge($this->jsonInput()));
    }

    public function rescheduleChallenge(): void
    {
        $this->respond($this->calendarService->updateChallengeDate($this->jsonInput()));
    }

    public function cancelChallenge(): void
    {
        $this->respond($this->calendarService->cancelChallenge($this->jsonInput()));
    }

    public function storeRoutine(): void
    {
        $this->respond($this->calendarService->createRoutine($this->jsonInput()));
    }

    public function goals(): void
    {
        Goal::refreshActiveProgress();

        Response::json([
            'ok' => true,
            'goal_types' => Goal::goalTypes(),
            'period_types' => Goal::periodTypes(),
            'goals' => array_map([$this, 'goalResource'], Goal::dashboardActive(100)),
        ]);
    }

    public function goalOptions(): void
    {
        Response::json([
            'ok' => true,
            'goal_types' => Goal::goalTypes(),
            'period_types' => Goal::periodTypes(),
            'platforms' => array_map([$this, 'platformResource'], Platform::active()),
            'languages' => array_map([$this, 'languageResource'], Language::active()),
        ]);
    }

    public function storeGoal(): void
    {
        $result = $this->goalService->create($this->jsonInput());
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'No se pudo crear la meta.',
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Meta creada correctamente.']);
    }

    public function updateGoal(): void
    {
        $result = $this->goalService->update($this->jsonInput());
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => $result['message'] ?? 'No se pudo actualizar la meta.',
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Meta actualizada correctamente.']);
    }

    public function updateTheme(): void
    {
        $user = Auth::user();
        $theme = (string) ($this->jsonInput()['theme'] ?? '');
        if (!$user || !in_array($theme, ['light', 'dark'], true)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo actualizar el tema.']);
            return;
        }

        User::updateTheme((int) $user['id'], $theme);
        Response::json(['ok' => true, 'message' => 'Tema actualizado.']);
    }

    /** @param array<string, mixed> $goal @return array<string, mixed> */
    private function goalResource(array $goal): array
    {
        return [
            'id' => (int) ($goal['id'] ?? 0),
            'goal_type' => (string) ($goal['goal_type'] ?? ''),
            'period_type' => (string) ($goal['period_type'] ?? ''),
            'target_value' => (int) ($goal['target_value'] ?? 0),
            'current_value' => (int) ($goal['current_value'] ?? 0),
            'progress_percent' => (float) ($goal['progress_percent'] ?? 0),
            'platform_name' => (string) ($goal['platform_name'] ?? ''),
            'language_name' => (string) ($goal['language_name'] ?? ''),
            'period_start' => (string) ($goal['period_start'] ?? ''),
            'period_end' => (string) ($goal['period_end'] ?? ''),
            'auto_renew' => (int) ($goal['auto_renew'] ?? 0) === 1,
        ];
    }

    /** @param array<string, mixed> $challenge @return array<string, mixed> */
    private function challengeResource(array $challenge): array
    {
        $id = (int) ($challenge['id'] ?? 0);
        $detail = $challenge;
        if ($id > 0 && (!array_key_exists('language_ids', $detail) || !array_key_exists('github_links', $detail))) {
            $detail = array_merge($detail, Challenge::detail($id) ?? []);
        }

        return [
            'id' => $id,
            'platform_id' => (int) ($detail['platform_id'] ?? 0),
            'platform_name' => (string) ($detail['platform_name'] ?? ''),
            'title' => (string) ($detail['title'] ?? ''),
            'scheduled_date' => (string) ($detail['scheduled_date'] ?? ''),
            'completed_date' => $detail['completed_date'] ?? null,
            'status' => (string) ($detail['status'] ?? ''),
            'difficulty' => (string) ($detail['difficulty'] ?? ''),
            'challenge_url' => safe_url($detail['challenge_url'] ?? null),
            'time_spent_minutes' => (int) ($detail['time_spent_minutes'] ?? 0),
            'notes' => (string) ($detail['notes'] ?? ''),
            'language_ids' => array_values(array_map('intval', $detail['language_ids'] ?? [])),
            'language_names' => $this->challengeLanguageNames($detail['language_ids'] ?? []),
            'github_links' => $this->githubLinksResource($detail['github_links'] ?? []),
            'origin' => (string) ($detail['origin'] ?? ''),
            'is_rescheduled' => (int) ($detail['is_rescheduled'] ?? 0) === 1,
        ];
    }

    /** @param array<string, mixed> $platform @return array<string, mixed> */
    private function platformResource(array $platform): array
    {
        return [
            'id' => (int) ($platform['id'] ?? 0),
            'name' => (string) ($platform['name'] ?? ''),
        ];
    }

    /** @param array<string, mixed> $notification @return array<string, mixed> */
    private function notificationResource(array $notification): array
    {
        return [
            'id' => (int) ($notification['id'] ?? 0),
            'type' => (string) ($notification['type'] ?? ''),
            'title' => (string) ($notification['title'] ?? ''),
            'message' => (string) ($notification['message'] ?? ''),
            'is_read' => (int) ($notification['is_read'] ?? 0) === 1,
            'action_url' => safe_app_url($notification['action_url'] ?? '', ''),
            'created_at' => (string) ($notification['created_at'] ?? ''),
        ];
    }

    /** @param array<string, mixed> $row @return array<string, mixed> */
    private function summarySeriesResource(array $row): array
    {
        return [
            'label' => (string) ($row['label'] ?? ''),
            'value' => (int) ($row['value'] ?? $row['completed'] ?? 0),
            'minutes' => (int) ($row['minutes'] ?? 0),
            'scheduled' => (int) ($row['scheduled'] ?? 0),
            'completed' => (int) ($row['completed'] ?? 0),
            'percent' => (float) ($row['percent'] ?? 0),
        ];
    }

    /** @param array<string, mixed> $language @return array<string, mixed> */
    private function languageResource(array $language): array
    {
        return [
            'id' => (int) ($language['id'] ?? 0),
            'name' => (string) ($language['name'] ?? ''),
        ];
    }

    /** @param mixed $languageIds @return string */
    private function challengeLanguageNames(mixed $languageIds): string
    {
        $ids = array_values(array_unique(array_filter(array_map('intval', is_array($languageIds) ? $languageIds : []))));
        if (!$ids) {
            return '';
        }

        $names = array_filter(array_map(
            static fn (array $language): string => in_array((int) ($language['id'] ?? 0), $ids, true)
                ? (string) ($language['name'] ?? '')
                : '',
            Language::all()
        ));

        return implode(', ', $names);
    }

    /** @param mixed $links @return array<int, array<string, string>> */
    private function githubLinksResource(mixed $links): array
    {
        if (!is_array($links)) {
            return [];
        }

        return array_values(array_filter(array_map(static function (mixed $link): ?array {
            if (is_array($link)) {
                $url = safe_url($link['github_url'] ?? null);
                if ($url === null) {
                    return null;
                }

                return [
                    'github_url' => $url,
                    'description' => (string) ($link['description'] ?? ''),
                ];
            }

            $url = safe_url($link);
            return $url === null ? null : ['github_url' => $url, 'description' => ''];
        }, $links)));
    }

    /**
     * @param array<string, mixed> $input
     * @return array{month: string, status: string}
     */
    private function challengeFilters(array $input): array
    {
        $month = (string) ($input['month'] ?? date('Y-m'));
        $status = (string) ($input['status'] ?? 'pending');

        return [
            'month' => preg_match('/^\d{4}-\d{2}$/', $month) === 1 ? $month : date('Y-m'),
            'status' => in_array($status, ['pending', 'completed', 'expired', 'missed', 'cancelled', 'all'], true) ? $status : 'pending',
        ];
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
