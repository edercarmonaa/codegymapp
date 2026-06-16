<?php

declare(strict_types=1);

use CodeGymApp\Services\SecurityLogService;

final class SecurityLogController
{
    public function __construct(private readonly SecurityLogService $securityLogService = new SecurityLogService())
    {
    }

    public function index(): void
    {
        $data = $this->securityLogService->indexPayload();
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('security/table', $data);
            return;
        }
        View::render('security/index', $data, 'main');
    }
}
