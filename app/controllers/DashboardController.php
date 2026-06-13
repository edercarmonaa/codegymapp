<?php

declare(strict_types=1);

final class DashboardController
{
    public function index(): void
    {
        Routine::generateCurrentMonth();
        Challenge::expirePending();
        Goal::refreshActiveProgress();
        Notification::generateSystemNotifications();
        $stats = Challenge::dashboardStats();
        $scheduled = max(1, (int) $stats['scheduled_month']);
        $stats['general_percent'] = round(((int) $stats['completed_month'] / $scheduled) * 100, 1);
        $stats['on_time_percent'] = round(((int) $stats['on_time_month'] / $scheduled) * 100, 1);

        View::render('dashboard/index', [
            'title' => 'Dashboard',
            'stats' => $stats,
            'streaks' => Challenge::streakStats(),
            'distribution' => Challenge::dashboardDistribution(),
            'weeklyCompliance' => Challenge::dashboardWeeklyCompliance(),
            'topPlatforms' => Challenge::dashboardTopPlatforms(),
            'topLanguages' => Challenge::dashboardTopLanguages(),
            'attention' => Challenge::dashboardAttention(),
            'goalAlerts' => Goal::dashboardAtRisk(),
            'activeGoals' => Goal::dashboardActive(),
            'goalTypes' => Goal::goalTypes(),
            'todayChallenges' => Challenge::todayPending(),
            'expiredChallenges' => Challenge::expiredForReview(),
            'notifications' => Notification::unread(5),
        ], 'main');
    }
}
