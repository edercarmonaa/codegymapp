<?php

declare(strict_types=1);

final class DashboardController
{
    public function index(): void
    {
        Challenge::expirePending();
        $stats = Challenge::dashboardStats();
        $scheduled = max(1, (int) $stats['scheduled_month']);
        $stats['general_percent'] = round(((int) $stats['completed_month'] / $scheduled) * 100, 1);
        $stats['on_time_percent'] = round(((int) $stats['on_time_month'] / $scheduled) * 100, 1);

        View::render('dashboard/index', [
            'title' => 'Dashboard',
            'stats' => $stats,
            'todayChallenges' => Challenge::todayPending(),
            'expiredChallenges' => Challenge::expiredForReview(),
        ], 'main');
    }
}

