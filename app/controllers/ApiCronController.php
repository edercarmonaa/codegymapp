<?php

declare(strict_types=1);

use CodeGymApp\Services\MobileReminderService;

final class ApiCronController
{
    public function __construct(
        private readonly MobileReminderService $mobileReminderService = new MobileReminderService()
    ) {
    }

    public function todayReminder(): void
    {
        if (!$this->authorized()) {
            http_response_code(403);
            Response::json(['ok' => false, 'message' => 'Cron no autorizado.']);
            return;
        }

        Response::json($this->mobileReminderService->sendTodayReminder($this->forceSend()));
    }

    public function expiredReviewReminder(): void
    {
        if (!$this->authorized()) {
            http_response_code(403);
            Response::json(['ok' => false, 'message' => 'Cron no autorizado.']);
            return;
        }

        Response::json($this->mobileReminderService->sendExpiredReviewReminder($this->forceSend()));
    }

    private function authorized(): bool
    {
        $secret = trim((string) Env::get('CRON_SECRET', ''));
        if ($secret === '') {
            return false;
        }

        $provided = trim((string) ($_GET['key'] ?? ($_SERVER['HTTP_X_CRON_SECRET'] ?? '')));
        return $provided !== '' && hash_equals($secret, $provided);
    }

    private function forceSend(): bool
    {
        return filter_var($_GET['force'] ?? false, FILTER_VALIDATE_BOOL);
    }
}
