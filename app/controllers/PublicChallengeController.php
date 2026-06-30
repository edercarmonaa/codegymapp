<?php

declare(strict_types=1);

use CodeGymApp\Services\PublicChallengeService;

final class PublicChallengeController
{
    public function __construct(private readonly PublicChallengeService $publicChallengeService = new PublicChallengeService())
    {
    }

    public function index(): void
    {
        $data = $this->publicChallengeService->indexPayload($_GET);
        View::render('public/completed_challenges', $data, 'public');
    }
}
