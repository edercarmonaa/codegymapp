<?php

declare(strict_types=1);

use CodeGymApp\Services\DashboardService;
use CodeGymApp\Services\ReportService;

final class DashboardController
{
    public function __construct(
        private readonly DashboardService $dashboardService = new DashboardService(),
        private readonly ReportService $reportService = new ReportService()
    ) {
    }

    public function index(): void
    {
        $this->dashboardService->refreshDashboardData();

        View::render('dashboard/index', array_merge(
            $this->dashboardService->dashboardPayload(),
            $this->reportService->reportPayload($_GET)
        ), 'main');
    }

    public function reportsTab(): void
    {
        $this->reportService->refreshReportData();

        View::render('dashboard/_reports_tab', array_merge($this->reportService->reportPayload($_GET), [
            'reportsTabActive' => true,
        ]));
    }
}
