<?php

declare(strict_types=1);

abstract class BaseModel
{
    protected static function db(): PDO
    {
        return Database::connection();
    }
}

