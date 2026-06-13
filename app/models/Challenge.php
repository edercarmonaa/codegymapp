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
        $stmt = self::db()->prepare('SELECT c.*, p.name AS platform_name FROM challenges c JOIN platforms p ON p.id = c.platform_id WHERE c.id = :id LIMIT 1');
        $stmt->execute(['id' => $id]);
        $challenge = $stmt->fetch();
        return is_array($challenge) ? $challenge : null;
    }

    /** @return array<string, mixed>|null */
    public static function detail(int $id): ?array
    {
        $challenge = self::find($id);
        if (!$challenge) {
            return null;
        }

        $languageStmt = self::db()->prepare('SELECT language_id FROM challenge_languages WHERE challenge_id = :id ORDER BY language_id ASC');
        $languageStmt->execute(['id' => $id]);
        $challenge['language_ids'] = array_map('intval', array_column($languageStmt->fetchAll(), 'language_id'));

        $linkStmt = self::db()->prepare('SELECT github_url, description FROM challenge_github_links WHERE challenge_id = :id ORDER BY id ASC');
        $linkStmt->execute(['id' => $id]);
        $challenge['github_links'] = $linkStmt->fetchAll();

        return $challenge;
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

    /** @param array<string, mixed> $data */
    public static function saveDetails(int $id, array $data): bool
    {
        $challenge = self::find($id);
        if (!$challenge || ((int) $challenge['is_locked'] === 1 && (string) $challenge['status'] !== 'completed')) {
            return false;
        }

        $db = self::db();
        $db->beginTransaction();
        try {
            $stmt = $db->prepare(
                'UPDATE challenges
                 SET title = :title,
                     challenge_url = :challenge_url,
                     difficulty = :difficulty,
                     time_spent_minutes = :time_spent_minutes,
                     notes = :notes,
                     updated_at = NOW()
                 WHERE id = :id'
            );
            $stmt->execute([
                'id' => $id,
                'title' => self::blankToNull((string) ($data['title'] ?? '')),
                'challenge_url' => self::blankToNull((string) ($data['challenge_url'] ?? '')),
                'difficulty' => self::blankToNull((string) ($data['difficulty'] ?? '')),
                'time_spent_minutes' => self::positiveIntOrNull($data['time_spent_minutes'] ?? null),
                'notes' => self::blankToNull((string) ($data['notes'] ?? '')),
            ]);

            self::replaceLanguages($id, $data['language_ids'] ?? []);
            self::replaceGithubLinks($id, (string) ($data['github_links'] ?? ''));
            $db->commit();
            return true;
        } catch (Throwable $exception) {
            $db->rollBack();
            throw $exception;
        }
    }

    /** @return array<int, string> */
    public static function completionErrors(int $id): array
    {
        $detail = self::detail($id);
        if (!$detail) {
            return ['El reto no existe.'];
        }

        $errors = [];
        if (trim((string) ($detail['title'] ?? '')) === '') {
            $errors[] = 'Captura el nombre del reto.';
        }
        if (trim((string) ($detail['difficulty'] ?? '')) === '') {
            $errors[] = 'Captura la dificultad.';
        }
        if ((int) ($detail['time_spent_minutes'] ?? 0) <= 0) {
            $errors[] = 'Captura el tiempo invertido.';
        }
        if (empty($detail['language_ids'])) {
            $errors[] = 'Selecciona al menos un lenguaje.';
        }

        return $errors;
    }

    public static function complete(int $id): bool
    {
        $challenge = self::find($id);
        if (!$challenge || !in_array((string) $challenge['status'], ['pending', 'expired'], true)) {
            return false;
        }

        $stmt = self::db()->prepare(
            "UPDATE challenges
             SET status = 'completed',
                 completed_date = CURDATE(),
                 is_locked = 0,
                 updated_at = NOW()
             WHERE id = :id"
        );
        return $stmt->execute(['id' => $id]);
    }

    public static function miss(int $id): bool
    {
        $challenge = self::find($id);
        if (!$challenge || !in_array((string) $challenge['status'], ['pending', 'expired'], true)) {
            return false;
        }

        $stmt = self::db()->prepare("UPDATE challenges SET status = 'missed', is_locked = 1, updated_at = NOW() WHERE id = :id");
        return $stmt->execute(['id' => $id]);
    }

    public static function cancel(int $id): bool
    {
        $challenge = self::find($id);
        if (!$challenge || !in_array((string) $challenge['status'], ['pending', 'expired'], true)) {
            return false;
        }

        $stmt = self::db()->prepare("UPDATE challenges SET status = 'cancelled', is_locked = 1, updated_at = NOW() WHERE id = :id");
        return $stmt->execute(['id' => $id]);
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

    /** @return array<string, int> */
    public static function dashboardDistribution(): array
    {
        return [
            'completed' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed' AND completed_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'missed' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'missed' AND scheduled_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'expired' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'expired' AND scheduled_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
            'cancelled' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'cancelled' AND scheduled_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())")->fetchColumn(),
        ];
    }

    /** @return array<string, int> */
    public static function streakStats(): array
    {
        $scheduledDates = self::dateSet(
            "SELECT DISTINCT scheduled_date AS date_value
             FROM challenges
             WHERE origin IN ('calendar', 'routine') AND scheduled_date <= CURDATE()"
        );
        $completedDates = self::dateSet(
            "SELECT DISTINCT completed_date AS date_value
             FROM challenges
             WHERE origin IN ('calendar', 'routine') AND status = 'completed' AND completed_date IS NOT NULL AND completed_date <= CURDATE()"
        );

        if (!$scheduledDates && !$completedDates) {
            return ['current' => 0, 'best' => 0, 'month' => 0];
        }

        $today = new DateTimeImmutable('today');
        $earliest = self::earliestDate(array_merge(array_keys($scheduledDates), array_keys($completedDates))) ?? $today;

        return [
            'current' => self::currentStreak($scheduledDates, $completedDates, $today, $earliest),
            'best' => self::bestStreak($scheduledDates, $completedDates, $today, $earliest),
            'month' => self::currentStreak($scheduledDates, $completedDates, $today, new DateTimeImmutable('first day of this month')),
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
    public static function allForList(array $filters = []): array
    {
        $where = [];
        $params = [];

        if (!empty($filters['status'])) {
            $where[] = 'c.status = :status';
            $params['status'] = $filters['status'];
        }

        if (!empty($filters['platform_id'])) {
            $where[] = 'c.platform_id = :platform_id';
            $params['platform_id'] = (int) $filters['platform_id'];
        }

        $sql = "SELECT
                c.*,
                p.name AS platform_name,
                (
                    SELECT GROUP_CONCAT(l.name ORDER BY l.name SEPARATOR ', ')
                    FROM challenge_languages cl
                    JOIN languages l ON l.id = cl.language_id
                    WHERE cl.challenge_id = c.id
                ) AS language_names,
                (
                    SELECT COUNT(*)
                    FROM challenge_github_links gl
                    WHERE gl.challenge_id = c.id
                ) AS github_count,
                (
                    SELECT GROUP_CONCAT(gl.github_url ORDER BY gl.id SEPARATOR '\n')
                    FROM challenge_github_links gl
                    WHERE gl.challenge_id = c.id
                ) AS github_urls
            FROM challenges c
            JOIN platforms p ON p.id = c.platform_id";

        if ($where) {
            $sql .= ' WHERE ' . implode(' AND ', $where);
        }

        $sql .= ' ORDER BY c.scheduled_date DESC, c.id DESC LIMIT 100';

        $stmt = self::db()->prepare($sql);
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    /** @return array<string, string> */
    public static function statusLabels(): array
    {
        return [
            'pending' => 'Pendiente',
            'completed' => 'Cumplido',
            'expired' => 'Vencido',
            'missed' => 'No cumplido',
            'cancelled' => 'Cancelado',
        ];
    }

    /** @return array<string, string> */
    public static function statusBadgeClasses(): array
    {
        return [
            'pending' => 'text-bg-primary',
            'completed' => 'text-bg-success',
            'expired' => 'text-bg-secondary',
            'missed' => 'text-bg-danger',
            'cancelled' => 'text-bg-dark',
        ];
    }

    /** @return array<string, mixed> */
    public static function reportData(): array
    {
        return [
            'compliance' => self::reportCompliance(),
            'timeByMonth' => self::reportTimeByMonth(),
            'platforms' => self::reportCompletedByPlatform(),
            'languages' => self::reportCompletedByLanguage(),
            'punctuality' => self::reportPunctuality(),
            'history' => self::reportHistoryTotals(),
        ];
    }

    /** @return array<string, float|int> */
    private static function reportCompliance(): array
    {
        $scheduled = (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE origin IN ('calendar', 'routine')")->fetchColumn();
        $completed = (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE origin IN ('calendar', 'routine') AND status = 'completed'")->fetchColumn();
        $onTime = (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE origin IN ('calendar', 'routine') AND status = 'completed' AND completed_date = scheduled_date")->fetchColumn();

        return [
            'scheduled' => $scheduled,
            'completed' => $completed,
            'on_time' => $onTime,
            'general_percent' => $scheduled > 0 ? round(($completed / $scheduled) * 100, 2) : 0,
            'on_time_percent' => $scheduled > 0 ? round(($onTime / $scheduled) * 100, 2) : 0,
        ];
    }

    /** @return array<int, array<string, mixed>> */
    private static function reportTimeByMonth(): array
    {
        $stmt = self::db()->query(
            "SELECT label, value
             FROM (
                 SELECT DATE_FORMAT(completed_date, '%Y-%m') AS label, COALESCE(SUM(time_spent_minutes), 0) AS value
                 FROM challenges
                 WHERE status = 'completed' AND completed_date IS NOT NULL
                 GROUP BY DATE_FORMAT(completed_date, '%Y-%m')
                 ORDER BY label DESC
                 LIMIT 12
             ) monthly
             ORDER BY label ASC"
        );
        return $stmt->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    private static function reportCompletedByPlatform(): array
    {
        $stmt = self::db()->query(
            "SELECT p.name AS label, COUNT(*) AS value
             FROM challenges c
             JOIN platforms p ON p.id = c.platform_id
             WHERE c.status = 'completed'
             GROUP BY p.id, p.name
             ORDER BY value DESC, p.name ASC
             LIMIT 10"
        );
        return $stmt->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    private static function reportCompletedByLanguage(): array
    {
        $stmt = self::db()->query(
            "SELECT l.name AS label, COUNT(DISTINCT c.id) AS value
             FROM challenges c
             JOIN challenge_languages cl ON cl.challenge_id = c.id
             JOIN languages l ON l.id = cl.language_id
             WHERE c.status = 'completed'
             GROUP BY l.id, l.name
             ORDER BY value DESC, l.name ASC
             LIMIT 10"
        );
        return $stmt->fetchAll();
    }

    /** @return array<string, int> */
    private static function reportPunctuality(): array
    {
        return [
            'on_time' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed' AND completed_date = scheduled_date")->fetchColumn(),
            'late' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed' AND completed_date > scheduled_date")->fetchColumn(),
        ];
    }

    /** @return array<string, int> */
    private static function reportHistoryTotals(): array
    {
        return [
            'completed' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'completed'")->fetchColumn(),
            'missed' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'missed'")->fetchColumn(),
            'expired' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'expired'")->fetchColumn(),
            'cancelled' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'cancelled'")->fetchColumn(),
            'pending' => (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'pending'")->fetchColumn(),
        ];
    }

    private static function validDate(?string $date): bool
    {
        if (!$date) {
            return false;
        }

        $parsed = DateTimeImmutable::createFromFormat('Y-m-d', substr($date, 0, 10));
        return $parsed instanceof DateTimeImmutable && $parsed->format('Y-m-d') === substr($date, 0, 10);
    }

    private static function blankToNull(string $value): ?string
    {
        $value = trim($value);
        return $value === '' ? null : $value;
    }

    private static function positiveIntOrNull(mixed $value): ?int
    {
        $int = (int) $value;
        return $int > 0 ? $int : null;
    }

    /** @return array<string, bool> */
    private static function dateSet(string $sql): array
    {
        $dates = [];
        foreach (self::db()->query($sql)->fetchAll() as $row) {
            if (!empty($row['date_value'])) {
                $dates[(string) $row['date_value']] = true;
            }
        }
        return $dates;
    }

    /** @param array<int, string> $dates */
    private static function earliestDate(array $dates): ?DateTimeImmutable
    {
        $dates = array_filter($dates);
        if (!$dates) {
            return null;
        }
        sort($dates);
        return new DateTimeImmutable((string) $dates[0]);
    }

    /** @param array<string, bool> $scheduledDates @param array<string, bool> $completedDates */
    private static function currentStreak(array $scheduledDates, array $completedDates, DateTimeImmutable $today, DateTimeImmutable $stopAt): int
    {
        $streak = 0;
        for ($day = $today; $day >= $stopAt; $day = $day->modify('-1 day')) {
            $key = $day->format('Y-m-d');
            if (isset($completedDates[$key])) {
                $streak++;
                continue;
            }
            if (isset($scheduledDates[$key]) && $key < $today->format('Y-m-d')) {
                break;
            }
        }
        return $streak;
    }

    /** @param array<string, bool> $scheduledDates @param array<string, bool> $completedDates */
    private static function bestStreak(array $scheduledDates, array $completedDates, DateTimeImmutable $today, DateTimeImmutable $startAt): int
    {
        $current = 0;
        $best = 0;
        for ($day = $startAt; $day <= $today; $day = $day->modify('+1 day')) {
            $key = $day->format('Y-m-d');
            if (isset($completedDates[$key])) {
                $current++;
                $best = max($best, $current);
                continue;
            }
            if (isset($scheduledDates[$key]) && $key < $today->format('Y-m-d')) {
                $current = 0;
            }
        }
        return $best;
    }

    /** @param mixed $languageIds */
    private static function replaceLanguages(int $challengeId, mixed $languageIds): void
    {
        $ids = array_values(array_unique(array_filter(array_map('intval', is_array($languageIds) ? $languageIds : []))));
        $db = self::db();
        $delete = $db->prepare('DELETE FROM challenge_languages WHERE challenge_id = :challenge_id');
        $delete->execute(['challenge_id' => $challengeId]);

        if (!$ids) {
            return;
        }

        $insert = $db->prepare('INSERT INTO challenge_languages (challenge_id, language_id) SELECT :challenge_id, id FROM languages WHERE id = :language_id');
        foreach ($ids as $languageId) {
            $insert->execute(['challenge_id' => $challengeId, 'language_id' => $languageId]);
        }
    }

    private static function replaceGithubLinks(int $challengeId, string $links): void
    {
        $db = self::db();
        $delete = $db->prepare('DELETE FROM challenge_github_links WHERE challenge_id = :challenge_id');
        $delete->execute(['challenge_id' => $challengeId]);

        $rows = preg_split('/\R+/', trim($links)) ?: [];
        $insert = $db->prepare('INSERT INTO challenge_github_links (challenge_id, github_url, created_at) VALUES (:challenge_id, :github_url, NOW())');
        foreach ($rows as $row) {
            $url = trim($row);
            if ($url === '') {
                continue;
            }
            $insert->execute(['challenge_id' => $challengeId, 'github_url' => $url]);
        }
    }
}
