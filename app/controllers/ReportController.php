<?php

declare(strict_types=1);

final class ReportController
{
    public function index(): void
    {
        $query = http_build_query($_GET);
        Response::redirect('/dashboard' . ($query !== '' ? '?' . $query : '') . '#reportes');
    }
}
