<?php

declare(strict_types=1);

final class SecurityLog extends BaseModel
{
    public static function record(?int $userId, string $eventType, string $result, string $description): void
    {
        try {
            $stmt = self::db()->prepare(
                'INSERT INTO security_logs (user_id, event_type, ip_address, user_agent, result, description, created_at)
                 VALUES (:user_id, :event_type, :ip_address, :user_agent, :result, :description, NOW())'
            );
            $stmt->execute([
                'user_id' => $userId,
                'event_type' => $eventType,
                'ip_address' => $_SERVER['REMOTE_ADDR'] ?? '',
                'user_agent' => substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 500),
                'result' => $result,
                'description' => $description,
            ]);
        } catch (Throwable) {
            // La bitácora no debe romper el flujo principal si la base aún no está instalada.
        }
    }

    /** @return array<int, array<string, mixed>> */
    public static function paginate(int $limit = 20, int $offset = 0): array
    {
        $stmt = self::db()->prepare('SELECT sl.*, u.username FROM security_logs sl LEFT JOIN users u ON u.id = sl.user_id ORDER BY sl.created_at DESC LIMIT :limit OFFSET :offset');
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt->fetchAll();
    }
}

