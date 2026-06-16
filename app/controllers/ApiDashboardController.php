<?php

declare(strict_types=1);

use CodeGymApp\Services\DashboardService;

final class ApiDashboardController
{
    public function __construct(private readonly DashboardService $dashboardService = new DashboardService())
    {
    }

    public function summary(): void
    {
        $this->dashboardService->refreshDashboardData();
        $payload = $this->dashboardService->dashboardPayload();
        unset($payload['title']);

        Response::json([
            'ok' => true,
            'dashboard' => $payload,
        ]);
    }
}
