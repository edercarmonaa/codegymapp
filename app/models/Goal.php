<?php

declare(strict_types=1);

final class Goal extends BaseModel
{
    /** @return array<string, string> */
    public static function goalTypes(): array
    {
        return [
            'completed_challenges' => 'Retos cumplidos',
            'practice_time' => 'Tiempo practicado',
            'streak' => 'Racha',
        ];
    }

    /** @return array<string, string> */
    public static function periodTypes(): array
    {
        return [
            'weekly' => 'Semanal',
            'monthly' => 'Mensual',
            'annual' => 'Anual',
        ];
    }

    /** @param array<string, mixed> $data */
    public static function create(array $data): void
    {
        $goalType = in_array($data['goal_type'] ?? '', array_keys(self::goalTypes()), true) ? (string) $data['goal_type'] : 'completed_challenges';
        $periodType = in_array($data['period_type'] ?? '', array_keys(self::periodTypes()), true) ? (string) $data['period_type'] : 'monthly';
        [$start, $end] = self::periodRange($periodType);

        $stmt = self::db()->prepare(
            "INSERT INTO goals (
                goal_type, period_type, target_value, platform_id, language_id,
                period_start, period_end, current_value, progress_percent,
                status, auto_renew, created_at, updated_at
             ) VALUES (
                :goal_type, :period_type, :target_value, :platform_id, :language_id,
                :period_start, :period_end, 0, 0,
                'active', :auto_renew, NOW(), NOW()
             )"
        );
        $stmt->execute([
            'goal_type' => $goalType,
            'period_type' => $periodType,
            'target_value' => (int) $data['target_value'],
            'platform_id' => self::nullableId($data['platform_id'] ?? 0),
            'language_id' => self::nullableId($data['language_id'] ?? 0),
            'period_start' => $start,
            'period_end' => $end,
            'auto_renew' => (int) ($data['auto_renew'] ?? 0),
        ]);
    }

    public static function refreshActiveProgress(): void
    {
        foreach (self::activeGoals() as $goal) {
            if (strtotime((string) $goal['period_end']) < strtotime(date('Y-m-d'))) {
                self::closeWithFinalProgress($goal);
                continue;
            }

            $current = self::currentValue($goal);
            $percent = self::progressPercent($current, (int) $goal['target_value']);
            self::updateProgress((int) $goal['id'], $current, $percent);
        }
    }

    /** @return array<int, array<string, mixed>> */
    public static function allForList(): array
    {
        $sql = "SELECT g.*, p.name AS platform_name, l.name AS language_name
            FROM goals g
            LEFT JOIN platforms p ON p.id = g.platform_id
            LEFT JOIN languages l ON l.id = g.language_id
            ORDER BY g.status ASC, g.period_end ASC, g.id DESC";
        return self::db()->query($sql)->fetchAll();
    }

    public static function close(int $id): void
    {
        if ($id <= 0) {
            return;
        }
        $stmt = self::db()->prepare("UPDATE goals SET status = 'closed', closed_at = COALESCE(closed_at, NOW()), updated_at = NOW() WHERE id = :id");
        $stmt->execute(['id' => $id]);
    }

    /** @return array<int, array<string, mixed>> */
    public static function dashboardAtRisk(): array
    {
        return self::db()->query(
            "SELECT g.*, p.name AS platform_name, l.name AS language_name
             FROM goals g
             LEFT JOIN platforms p ON p.id = g.platform_id
             LEFT JOIN languages l ON l.id = g.language_id
             WHERE g.status = 'active'
               AND g.progress_percent < 50
             ORDER BY g.period_end ASC, g.progress_percent ASC
             LIMIT 3"
        )->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    private static function activeGoals(): array
    {
        return self::db()->query("SELECT * FROM goals WHERE status = 'active' ORDER BY period_end ASC")->fetchAll();
    }

    /** @param array<string, mixed> $goal */
    private static function closeWithFinalProgress(array $goal): void
    {
        $current = self::currentValue($goal);
        $percent = self::progressPercent($current, (int) $goal['target_value']);
        $stmt = self::db()->prepare(
            "UPDATE goals
             SET current_value = :current_value,
                 progress_percent = :progress_percent,
                 status = 'closed',
                 closed_at = NOW(),
                 updated_at = NOW()
             WHERE id = :id"
        );
        $stmt->execute([
            'id' => $goal['id'],
            'current_value' => $current,
            'progress_percent' => $percent,
        ]);

        if ((int) $goal['auto_renew'] === 1) {
            self::renew($goal);
        }
    }

    /** @param array<string, mixed> $goal */
    private static function renew(array $goal): void
    {
        [$start, $end] = self::nextPeriodRange((string) $goal['period_type'], (string) $goal['period_end']);
        $stmt = self::db()->prepare(
            "INSERT INTO goals (
                goal_type, period_type, target_value, platform_id, language_id,
                period_start, period_end, current_value, progress_percent,
                status, auto_renew, source_goal_id, created_at, updated_at
             ) VALUES (
                :goal_type, :period_type, :target_value, :platform_id, :language_id,
                :period_start, :period_end, 0, 0,
                'active', :auto_renew, :source_goal_id, NOW(), NOW()
             )"
        );
        $stmt->execute([
            'goal_type' => $goal['goal_type'],
            'period_type' => $goal['period_type'],
            'target_value' => $goal['target_value'],
            'platform_id' => $goal['platform_id'],
            'language_id' => $goal['language_id'],
            'period_start' => $start,
            'period_end' => $end,
            'auto_renew' => $goal['auto_renew'],
            'source_goal_id' => $goal['id'],
        ]);
    }

    /** @param array<string, mixed> $goal */
    private static function currentValue(array $goal): int
    {
        if ((string) $goal['goal_type'] === 'streak') {
            return self::filteredCurrentStreak($goal);
        }

        $joins = '';
        $where = [
            "c.status = 'completed'",
            'c.completed_date BETWEEN :start AND :end',
        ];
        $params = [
            'start' => $goal['period_start'],
            'end' => $goal['period_end'],
        ];

        if (!empty($goal['platform_id'])) {
            $where[] = 'c.platform_id = :platform_id';
            $params['platform_id'] = (int) $goal['platform_id'];
        }

        if (!empty($goal['language_id'])) {
            $joins .= ' JOIN challenge_languages cl ON cl.challenge_id = c.id';
            $where[] = 'cl.language_id = :language_id';
            $params['language_id'] = (int) $goal['language_id'];
        }

        $select = (string) $goal['goal_type'] === 'practice_time'
            ? 'COALESCE(SUM(c.time_spent_minutes), 0)'
            : 'COUNT(DISTINCT c.id)';

        $stmt = self::db()->prepare('SELECT ' . $select . ' FROM challenges c' . $joins . ' WHERE ' . implode(' AND ', $where));
        $stmt->execute($params);
        return (int) $stmt->fetchColumn();
    }

    /** @param array<string, mixed> $goal */
    private static function filteredCurrentStreak(array $goal): int
    {
        [$joins, $where, $params] = self::challengeFilterParts($goal, 'scheduled_date');
        $scheduled = self::goalDateSet(
            'SELECT DISTINCT c.scheduled_date AS date_value FROM challenges c' . $joins . ' WHERE ' . implode(' AND ', $where),
            $params
        );

        [$joins, $where, $params] = self::challengeFilterParts($goal, 'completed_date');
        $where[] = "c.status = 'completed'";
        $where[] = 'c.completed_date IS NOT NULL';
        $completed = self::goalDateSet(
            'SELECT DISTINCT c.completed_date AS date_value FROM challenges c' . $joins . ' WHERE ' . implode(' AND ', $where),
            $params
        );

        $today = new DateTimeImmutable('today');
        $stopAt = new DateTimeImmutable((string) $goal['period_start']);
        $streak = 0;

        for ($day = $today; $day >= $stopAt; $day = $day->modify('-1 day')) {
            $key = $day->format('Y-m-d');
            if (isset($completed[$key])) {
                $streak++;
                continue;
            }
            if (isset($scheduled[$key]) && $key < $today->format('Y-m-d')) {
                break;
            }
        }

        return $streak;
    }

    /** @return array{0: string, 1: array<int, string>, 2: array<string, mixed>} */
    private static function challengeFilterParts(array $goal, string $dateColumn): array
    {
        $joins = '';
        $where = [
            "c.origin IN ('calendar', 'routine')",
            'c.' . $dateColumn . ' BETWEEN :start AND :end',
            'c.' . $dateColumn . ' <= CURDATE()',
        ];
        $params = [
            'start' => $goal['period_start'],
            'end' => $goal['period_end'],
        ];

        if (!empty($goal['platform_id'])) {
            $where[] = 'c.platform_id = :platform_id';
            $params['platform_id'] = (int) $goal['platform_id'];
        }

        if (!empty($goal['language_id'])) {
            $joins .= ' JOIN challenge_languages cl_streak ON cl_streak.challenge_id = c.id';
            $where[] = 'cl_streak.language_id = :language_id';
            $params['language_id'] = (int) $goal['language_id'];
        }

        return [$joins, $where, $params];
    }

    /** @param array<string, mixed> $params @return array<string, bool> */
    private static function goalDateSet(string $sql, array $params): array
    {
        $stmt = self::db()->prepare($sql);
        $stmt->execute($params);
        $dates = [];
        foreach ($stmt->fetchAll() as $row) {
            if (!empty($row['date_value'])) {
                $dates[(string) $row['date_value']] = true;
            }
        }
        return $dates;
    }

    private static function updateProgress(int $id, int $current, float $percent): void
    {
        $stmt = self::db()->prepare('UPDATE goals SET current_value = :current_value, progress_percent = :progress_percent, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'current_value' => $current, 'progress_percent' => $percent]);
    }

    private static function progressPercent(int $current, int $target): float
    {
        if ($target <= 0) {
            return 0.0;
        }
        return round(min(100, ($current / $target) * 100), 2);
    }

    /** @return array{0: string, 1: string} */
    private static function periodRange(string $periodType): array
    {
        return match ($periodType) {
            'weekly' => [date('Y-m-d', strtotime('monday this week')), date('Y-m-d', strtotime('sunday this week'))],
            'annual' => [date('Y-01-01'), date('Y-12-31')],
            default => [date('Y-m-01'), date('Y-m-t')],
        };
    }

    /** @return array{0: string, 1: string} */
    private static function nextPeriodRange(string $periodType, string $periodEnd): array
    {
        $next = strtotime($periodEnd . ' +1 day');
        if ($periodType === 'weekly') {
            return [date('Y-m-d', $next), date('Y-m-d', strtotime('+6 days', $next))];
        }
        if ($periodType === 'annual') {
            return [date('Y-01-01', $next), date('Y-12-31', $next)];
        }
        return [date('Y-m-01', $next), date('Y-m-t', $next)];
    }

    private static function nullableId(mixed $value): ?int
    {
        $id = (int) $value;
        return $id > 0 ? $id : null;
    }
}
