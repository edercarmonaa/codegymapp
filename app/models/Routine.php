<?php

declare(strict_types=1);

final class Routine extends BaseModel
{
    /** @param array<string, mixed> $data */
    public static function create(array $data): void
    {
        $stmt = self::db()->prepare(
            "INSERT INTO routines (
                platform_id, frequency_type, week_days, month_day,
                start_date, end_date, is_active, created_at, updated_at
            ) VALUES (
                :platform_id, :frequency_type, :week_days, :month_day,
                :start_date, :end_date, 1, NOW(), NOW()
            )"
        );
        $stmt->execute([
            'platform_id' => (int) $data['platform_id'],
            'frequency_type' => $data['frequency_type'],
            'week_days' => self::weekDaysValue($data['week_days'] ?? []),
            'month_day' => self::monthDayValue($data['month_day'] ?? null),
            'start_date' => $data['start_date'],
            'end_date' => self::blankToNull((string) ($data['end_date'] ?? '')),
        ]);
    }

    public static function disable(int $id): void
    {
        $stmt = self::db()->prepare('UPDATE routines SET is_active = 0, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id]);

        $stmt = self::db()->prepare(
            "UPDATE challenges
             SET status = 'cancelled', is_locked = 1, updated_at = NOW()
             WHERE routine_id = :id
               AND status = 'pending'
               AND scheduled_date >= CURDATE()"
        );
        $stmt->execute(['id' => $id]);
    }

    /** @return array<int, array<string, mixed>> */
    public static function allForList(): array
    {
        return self::db()->query(
            'SELECT r.*, p.name AS platform_name
             FROM routines r
             JOIN platforms p ON p.id = r.platform_id
             ORDER BY r.is_active DESC, r.created_at DESC'
        )->fetchAll();
    }

    public static function generateCurrentMonth(): void
    {
        $monthStart = new DateTimeImmutable('first day of this month 00:00:00');
        $monthEnd = new DateTimeImmutable('last day of this month 00:00:00');

        foreach (self::activeForMonth($monthStart, $monthEnd) as $routine) {
            foreach (self::datesForRoutine($routine, $monthStart, $monthEnd) as $date) {
                self::createChallengeIfMissing($routine, $date);
            }
        }
    }

    /** @return array<int, array<string, mixed>> */
    private static function activeForMonth(DateTimeImmutable $monthStart, DateTimeImmutable $monthEnd): array
    {
        $stmt = self::db()->prepare(
            "SELECT * FROM routines
             WHERE is_active = 1
               AND start_date <= :month_end
               AND (end_date IS NULL OR end_date >= :month_start)"
        );
        $stmt->execute([
            'month_start' => $monthStart->format('Y-m-d'),
            'month_end' => $monthEnd->format('Y-m-d'),
        ]);
        return $stmt->fetchAll();
    }

    /** @param array<string, mixed> $routine @return array<int, string> */
    private static function datesForRoutine(array $routine, DateTimeImmutable $monthStart, DateTimeImmutable $monthEnd): array
    {
        $routineStart = new DateTimeImmutable((string) $routine['start_date']);
        $start = $routineStart > $monthStart ? $routineStart : $monthStart;
        $end = $monthEnd;
        if (!empty($routine['end_date'])) {
            $routineEnd = new DateTimeImmutable((string) $routine['end_date']);
            $end = $routineEnd < $end ? $routineEnd : $end;
        }

        $dates = [];
        for ($day = $start; $day <= $end; $day = $day->modify('+1 day')) {
            if (self::routineMatchesDate($routine, $day)) {
                $dates[] = $day->format('Y-m-d');
            }
        }
        return $dates;
    }

    /** @param array<string, mixed> $routine */
    private static function routineMatchesDate(array $routine, DateTimeImmutable $day): bool
    {
        $type = (string) $routine['frequency_type'];
        if ($type === 'daily') {
            return true;
        }
        if ($type === 'weekly') {
            $weekDays = array_filter(array_map('intval', explode(',', (string) $routine['week_days'])));
            return in_array((int) $day->format('N'), $weekDays, true);
        }
        if ($type === 'monthly') {
            return (int) $routine['month_day'] === (int) $day->format('j');
        }
        return false;
    }

    /** @param array<string, mixed> $routine */
    private static function createChallengeIfMissing(array $routine, string $date): void
    {
        $exists = self::db()->prepare('SELECT COUNT(*) FROM challenges WHERE routine_id = :routine_id AND scheduled_date = :scheduled_date');
        $exists->execute(['routine_id' => $routine['id'], 'scheduled_date' => $date]);
        if ((int) $exists->fetchColumn() > 0) {
            return;
        }

        $stmt = self::db()->prepare(
            "INSERT INTO challenges (
                platform_id, routine_id, scheduled_date, original_scheduled_date,
                status, origin, created_at, updated_at
             ) VALUES (
                :platform_id, :routine_id, :scheduled_date, :original_scheduled_date,
                'pending', 'routine', NOW(), NOW()
             )"
        );
        $stmt->execute([
            'platform_id' => $routine['platform_id'],
            'routine_id' => $routine['id'],
            'scheduled_date' => $date,
            'original_scheduled_date' => $date,
        ]);
    }

    /** @param mixed $weekDays */
    private static function weekDaysValue(mixed $weekDays): ?string
    {
        $days = array_values(array_unique(array_filter(array_map('intval', is_array($weekDays) ? $weekDays : []), static fn (int $day): bool => $day >= 1 && $day <= 7)));
        sort($days);
        return $days ? implode(',', $days) : null;
    }

    private static function monthDayValue(mixed $monthDay): ?int
    {
        $day = (int) $monthDay;
        return $day >= 1 && $day <= 31 ? $day : null;
    }

    private static function blankToNull(string $value): ?string
    {
        $value = trim($value);
        return $value === '' ? null : $value;
    }
}
