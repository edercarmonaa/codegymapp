<?php

declare(strict_types=1);

final class ReportController
{
    public function index(): void
    {
        Challenge::expirePending();
        View::render('reports/index', [
            'title' => 'Reportes',
            'reports' => Challenge::reportData(),
        ], 'main');
    }
}
