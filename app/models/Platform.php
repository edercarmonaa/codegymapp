<?php

declare(strict_types=1);

final class Platform extends BaseModel
{
    /** @return array<int, array<string, mixed>> */
    public static function all(): array
    {
        return self::db()->query('SELECT * FROM platforms ORDER BY is_active DESC, name ASC')->fetchAll();
    }

    /** @return array<int, array<string, mixed>> */
    public static function active(): array
    {
        return self::db()->query('SELECT * FROM platforms WHERE is_active = 1 ORDER BY name ASC')->fetchAll();
    }

    public static function existsActive(int $id): bool
    {
        $stmt = self::db()->prepare('SELECT COUNT(*) FROM platforms WHERE id = :id AND is_active = 1');
        $stmt->execute(['id' => $id]);
        return (int) $stmt->fetchColumn() > 0;
    }

    /** @param array<string, string> $data */
    public static function save(array $data): void
    {
        if (!empty($data['id'])) {
            $stmt = self::db()->prepare('UPDATE platforms SET name = :name, description = :description, url = :url, updated_at = NOW() WHERE id = :id');
            $stmt->execute(['id' => $data['id'], 'name' => $data['name'], 'description' => $data['description'], 'url' => $data['url']]);
            return;
        }

        $stmt = self::db()->prepare('INSERT INTO platforms (name, description, url, is_active, created_at, updated_at) VALUES (:name, :description, :url, 1, NOW(), NOW())');
        $stmt->execute(['name' => $data['name'], 'description' => $data['description'], 'url' => $data['url']]);
    }

    public static function setActive(int $id, bool $active): void
    {
        $stmt = self::db()->prepare('UPDATE platforms SET is_active = :active, updated_at = NOW() WHERE id = :id');
        $stmt->execute(['id' => $id, 'active' => $active ? 1 : 0]);
    }
}
