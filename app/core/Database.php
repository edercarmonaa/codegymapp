<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class Database
{
    private static ?\PDO $connection = null;

    public static function connection(): \PDO
    {
        if (self::$connection instanceof \PDO) {
            return self::$connection;
        }

        $host = Env::get('DB_HOST', 'localhost');
        $name = Env::get('DB_NAME', '');
        $charset = Env::get('DB_CHARSET', 'utf8mb4');
        $dsn = "mysql:host={$host};dbname={$name};charset={$charset}";

        self::$connection = new \PDO($dsn, (string) Env::get('DB_USER', ''), (string) Env::get('DB_PASS', ''), [
            \PDO::ATTR_ERRMODE => \PDO::ERRMODE_EXCEPTION,
            \PDO::ATTR_DEFAULT_FETCH_MODE => \PDO::FETCH_ASSOC,
            \PDO::ATTR_EMULATE_PREPARES => false,
        ]);

        return self::$connection;
    }

    public static function reconnect(): \PDO
    {
        self::$connection = null;
        return self::connection();
    }
}

if (!\class_exists('Database', false)) {
    \class_alias(Database::class, 'Database');
}
