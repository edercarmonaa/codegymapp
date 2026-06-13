<?php

declare(strict_types=1);

return [
    'host' => Env::get('DB_HOST', 'localhost'),
    'database' => Env::get('DB_NAME', ''),
    'username' => Env::get('DB_USER', ''),
    'charset' => Env::get('DB_CHARSET', 'utf8mb4'),
];

