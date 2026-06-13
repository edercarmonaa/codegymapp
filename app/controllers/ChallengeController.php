<?php

declare(strict_types=1);

final class ChallengeController
{
    public function index(): void
    {
        Challenge::expirePending();
        $filters = [
            'status' => (string) ($_GET['status'] ?? ''),
            'platform_id' => (int) ($_GET['platform_id'] ?? 0),
        ];

        View::render('challenges/index', [
            'title' => 'Retos',
            'challenges' => Challenge::allForList($filters),
            'platforms' => Platform::all(),
            'statusLabels' => Challenge::statusLabels(),
            'statusBadgeClasses' => Challenge::statusBadgeClasses(),
            'filters' => $filters,
        ], 'main');
    }
}
