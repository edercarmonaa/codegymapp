<?php

declare(strict_types=1);

final class ReportController
{
    public function index(): void
    {
        View::render('reports/index', ['title' => 'Reportes'], 'main');
    }
}

