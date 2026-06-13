<?php

declare(strict_types=1);

final class Language extends BaseModel
{
    /** @return array<int, array<string, mixed>> */
    public static function all(): array
    {
        return self::db()->query('SELECT * FROM languages ORDER BY is_active DESC, name ASC')->fetchAll();
    }

    /** @param array<string, mixed> $state @return array<int, array<string, mixed>> */
    public static function paginated(array $state): array
    {
        $columns = ['name' => 'name', 'is_active' => 'is_active', 'created_at' => 'created_at'];
        $orderBy = $columns[(string) $state['sort']] ?? 'name';
        $dir = (string) $state['dir'] === 'desc' ? 'DESC' : 'ASC';
        $stmt = self::db()->prepare("SELECT * FROM languages ORDER BY {$orderBy} {$dir}, id DESC LIMIT :limit OFFSET :offset");
        $stmt->bindValue(':limit', (int) $state['per_page'], PDO::PARAM_INT);
        $stmt->bindValue(':offset', (int) $state['offset'], PDO::PARAM_INT);
        $stmt->execute();
        return $stmt->fetchAll();
    }

    public static function countAll(): int
    {
        return (int) self::db()->query('SELECT COUNT(*) FROM languages')->fetchColumn();
    }

    /** @return array<int, array<string, mixed>> */
    public static function active(): array
    {
        return self::db()->query('SELECT * FROM languages WHERE is_active = 1 ORDER BY name ASC')->fetchAll();
    }

    /** @param array<string, string> $data */
    public static function save(array $data): void
    {
        if (!empty($data['id'])) {
            $stmt = self::db()->prepare('UPDATE languages SET name = :name, updated_at = NOW() WHERE id = :id');
            $stmt->execute(['id' => $data['id'], 'name' => $data['name']]);
            return;
        }

        $stmt = self::db()->prepare('INSERT INTO languages (name, is_active, created_at, updated_at) VALUES (:name, 1, NOW(), NOW())');
        $stmt->execute(['name' => $data['name']]);
    }

    public static function setActive(int $id, bool $active): void
    {
        $stmt = self::db()->prepare('UPDATE languages SET is_active = :active, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'active' => $active ? 1 : 0]);
    }
}
