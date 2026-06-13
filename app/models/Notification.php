<?php

declare(strict_types=1);

final class Notification extends BaseModel
{
    public static function generateSystemNotifications(): void
    {
        $todayCount = (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'pending' AND scheduled_date = CURDATE()")->fetchColumn();
        if ($todayCount > 0) {
            self::createOnceToday(
                'today_challenges',
                'Retos para hoy',
                'Tienes ' . $todayCount . ' reto' . ($todayCount === 1 ? '' : 's') . ' pendiente' . ($todayCount === 1 ? '' : 's') . ' para hoy.',
                '/calendario'
            );
        }

        $expiredCount = (int) self::db()->query("SELECT COUNT(*) FROM challenges WHERE status = 'expired'")->fetchColumn();
        if ($expiredCount > 0) {
            self::createOnceToday(
                'expired_challenges',
                'Retos vencidos',
                'Tienes ' . $expiredCount . ' reto' . ($expiredCount === 1 ? '' : 's') . ' vencido' . ($expiredCount === 1 ? '' : 's') . ' pendiente' . ($expiredCount === 1 ? '' : 's') . ' de revisar.',
                '/dashboard'
            );
        }
    }

    /** @return array<int, array<string, mixed>> */
    public static function allForList(): array
    {
        return self::db()->query('SELECT * FROM notifications WHERE is_active = 1 ORDER BY is_read ASC, created_at DESC LIMIT 100')->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    public static function unread(int $limit = 5): array
    {
        $stmt = self::db()->prepare('SELECT * FROM notifications WHERE is_active = 1 AND is_read = 0 ORDER BY created_at DESC LIMIT :limit');
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt->fetchAll();
    }

    public static function unreadCount(): int
    {
        try {
            return (int) self::db()->query('SELECT COUNT(*) FROM notifications WHERE is_active = 1 AND is_read = 0')->fetchColumn();
        } catch (Throwable) {
            return 0;
        }
    }

    public static function markRead(int $id): void
    {
        $stmt = self::db()->prepare('UPDATE notifications SET is_read = 1, read_at = COALESCE(read_at, NOW()) WHERE id = :id');
        $stmt->execute(['id' => $id]);
    }

    public static function delete(int $id): void
    {
        $stmt = self::db()->prepare('UPDATE notifications SET is_active = 0 WHERE id = :id AND is_read = 1');
        $stmt->execute(['id' => $id]);
    }

    private static function createOnceToday(string $type, string $title, string $message, string $actionUrl): void
    {
        $stmt = self::db()->prepare('SELECT COUNT(*) FROM notifications WHERE type = :type AND action_url = :action_url AND DATE(created_at) = CURDATE()');
        $stmt->execute(['type' => $type, 'action_url' => $actionUrl]);
        if ((int) $stmt->fetchColumn() > 0) {
            return;
        }

        $insert = self::db()->prepare(
            'INSERT INTO notifications (type, title, message, is_read, action_url, is_active, created_at)
             VALUES (:type, :title, :message, 0, :action_url, 1, NOW())'
        );
        $insert->execute([
            'type' => $type,
            'title' => $title,
            'message' => $message,
            'action_url' => $actionUrl,
        ]);
    }
}
