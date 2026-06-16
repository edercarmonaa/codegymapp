<?php

declare(strict_types=1);

use CodeGymApp\Services\ReportService;

final class ApiReportController
{
    public function __construct(private readonly ReportService $reportService = new ReportService())
    {
    }

    public function index(): void
    {
        $this->reportService->refreshReportData();
        Response::json([
            'ok' => true,
            'report' => $this->reportService->reportPayload($_GET),
        ]);
    }
}
