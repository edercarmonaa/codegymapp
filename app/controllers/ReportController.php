<?php

declare(strict_types=1);

final class ReportController
{
    public function index(): void
    {
        Challenge::expirePending();
        $filters = [
            'date_from' => substr((string) ($_GET['date_from'] ?? ''), 0, 10),
            'date_to' => substr((string) ($_GET['date_to'] ?? ''), 0, 10),
            'platform_id' => (int) ($_GET['platform_id'] ?? 0),
            'language_id' => (int) ($_GET['language_id'] ?? 0),
            'status' => (string) ($_GET['status'] ?? ''),
            'completion_type' => (string) ($_GET['completion_type'] ?? ''),
        ];
        View::render('reports/index', [
            'title' => 'Reportes',
            'reports' => Challenge::reportData($filters),
            'filters' => $filters,
            'platforms' => Platform::all(),
            'languages' => Language::all(),
            'statusLabels' => Challenge::statusLabels(),
        ], 'main');
    }
}
