<?php

declare(strict_types=1);

return [
    'name' => Env::get('APP_NAME', 'CodeGymApp'),
    'env' => Env::get('APP_ENV', 'production'),
    'debug' => Env::get('APP_DEBUG', false),
    'url' => Env::get('APP_URL', ''),
];

