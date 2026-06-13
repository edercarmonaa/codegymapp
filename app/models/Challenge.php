<?php

declare(strict_types=1);

final class Challenge extends BaseModel
{
    private const STATUS_COLORS = [
        'pending' => '#0d6efd',
        'completed' => '#198754',
        'expired' => '#6c757d',
        'missed' => '#dc3545',
        'cancelled' => '#842029',
    ];

    public static function expirePending(): void
    {
        self::db()->exec("UPDATE challenges SET status = 'expired', updated_at = NOW() WHERE status = 'pending' AND scheduled_date < CURDATE()");
    }

    /** @return array<int, array<string, mixed>> */
    public static function calendarEvents(?string $start, ?string $end): array
    {
        $sql = "SELECT c.*, p.name AS platform_name
            FROM challenges c
            JOIN platforms p ON p.id = c.platform_id
            WHERE c.scheduled_date BETWEEN :start AND :end
            ORDER BY c.scheduled_date ASC, c.id ASC";
        $stmt = self::db()->prepare($sql);
        $stmt->execute([
            'start' => self::validDate($start) ? $start : date('Y-m-01'),
            'end' => self::validDate($end) ? $end : date('Y-m-t'),
        ]);

        return array_map(static function (array $challenge): array {
            $status = (string) $challenge['status'];
            $title = (string) $challenge['platform_name'];
            if (!empty($challenge['title'])) {
                $title .= ' - ' . $challenge['title'];
            }
            if ((int) $challenge['is_rescheduled'] === 1 && $status === 'pending') {
                $title .= ' ↻';
            }

            return [
                'id' => (string) $challenge['id'],
                'title' => $title,
                'start' => $challenge['scheduled_date'],
                'allDay' => true,
                'editable' => self::canDrag($challenge),
                'backgroundColor' => self::STATUS_COLORS[$status] ?? '#6c757d',
                'borderColor' => self::STATUS_COLORS[$status] ?? '#6c757d',
                'extendedProps' => [
                    'status' => $status,
                    'platform' => $challenge['platform_name'],
                    'isRescheduled' => (bool) $challenge['is_rescheduled'],
                ],
            ];
        }, $stmt->fetchAll());
    }

    /** @return array<string, mixed>|null */
    public static function find(int $id): ?array
    {
        $stmt = self::db()->prepare('SELECT * FROM challenges WHERE id = :id LIMIT 1');
        $stmt->execute(['id' => $id]);
        $challenge = $stmt->fetch();
        return is_array($challenge) ? $challenge : null;
    }

    public static function createFromCalendar(int $platformId, string $scheduledDate): int
    {
        $stmt = self::db()->prepare(
            "INSERT INTO challenges (platform_id, scheduled_date, original_scheduled_date, status, origin, created_at, updated_at)
             VALUES (:platform_id, :scheduled_date, :original_scheduled_date, 'pending', 'calendar', NOW(), NOW())"
        );
        $stmt->execute([
            'platform_id' => $platformId,
            'scheduled_date' => $scheduledDate,
            'original_scheduled_date' => $scheduledDate,
        ]);

        return (int) self::db()->lastInsertId();
    }

    public static function updateScheduledDate(int $id, string $scheduledDate): bool
    {
        $challenge = self::find($id);
        if (!$challenge || !self::canDrag($challenge)) {
            return false;
        }

        $stmt = self::db()->prepare(
            'UPDATE challenges
             SET scheduled_date = :scheduled_date,
                 original_scheduled_date = COALESCE(original_scheduled_date, scheduled_date),
                 is_rescheduled = 1,
                 reschedule_count = reschedule_count + 1,
                 last_rescheduled_date = CURDATE(),
                 updated_at = NOW()
             WHERE id = :id'
        );

        return $stmt->execute(['id' => $id, 'scheduled_date' => $scheduledDate]);
    }

    /** @param array<string, mixed> $challenge */
    public static function canDrag(array $challenge): bool
    {
        return (string) $challenge['status'] === 'pending'
            && strtotime((string) $challenge['scheduled_date']) >= strtotime(date('Y-m-d'));
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

    private static function validDate(?string $date): bool
    {
        if (!$date) {
            return false;
        }

        $parsed = DateTimeImmutable::createFromFormat('Y-m-d', substr($date, 0, 10));
        return $parsed instanceof DateTimeImmutable && $parsed->format('Y-m-d') === substr($date, 0, 10);
    }
}
