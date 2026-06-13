<?php

declare(strict_types=1);

final class Challenge extends BaseModel
{
    public static function expirePending(): void
    {
        self::db()->exec("UPDATE challenges SET status = 'expired', updated_at = NOW() WHERE status = 'pending' AND scheduled_date < CURDATE()");
    }

    /** @return array<string, mixed> */
    public static function dashboardStats(): array
    {
        return [
            'completed_month' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed' AND completed_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'expired_review' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'expired'")->fetchColumn(),
            'time_month' => (int) self::db()->query("SELECT COALESCE(SUM(time_spent_minutes), 0) FROM challenges WHERE status = 'completed' AND completed_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'scheduled_month' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE origin IN ('calendar','routine') AND scheduled_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'on_time_month' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed' AND completed_date = scheduled_date AND completed_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
        ];
    }

    /** @return array<int, array<string, mixed>> */
    public static function todayPending(): array
    {
        return self::db()->query("SELECT c.*, p.name AS platform_name FROM challenges c JOIN platforms p ON p.id = c.platform_id WHERE c.status = 'pending' AND c.scheduled_date = CURDATE() ORDER BY c.id DESC")->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    public static function expiredForReview(): array
    {
        return self::db()->query("SELECT c.*, p.name AS platform_name FROM challenges c JOIN platforms p ON p.id = c.platform_id WHERE c.status = 'expired' ORDER BY c.scheduled_date ASC")->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    public static function allForList(): array
    {
        return self::db()->query("SELECT c.*, p.name AS platform_name FROM challenges c JOIN platforms p ON p.id = c.platform_id ORDER BY c.scheduled_date DESC, c.id DESC LIMIT 50")->fetchAll();
    }
}

