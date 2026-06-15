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
        $filters = [
            'date_from' => substr((string) ($_GET['date_from'] ?? ''), 0, 10),
            'date_to' => substr((string) ($_GET['date_to'] ?? ''), 0, 10),
            'platform_id' => (int) ($_GET['platform_id'] ?? 0),
            'language_id' => (int) ($_GET['language_id'] ?? 0),
            'status' => (string) ($_GET['status'] ?? ''),
            'completion_type' => (string) ($_GET['completion_type'] ?? ''),
        ];

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
            'reports' => Challenge::reportData($filters),
            'filters' => $filters,
            'platforms' => Platform::all(),
            'languages' => Language::all(),
            'statusLabels' => Challenge::statusLabels(),
        ], 'main');
    }
}
