<?php

declare(strict_types=1);

spl_autoload_register(static function (string $class): void {
    $base = dirname(__DIR__) . '/';
    $prefixes = [
        'CodeGymApp\\Core\\' => $base . 'core/',
        'CodeGymApp\\Controllers\\' => $base . 'controllers/',
        'CodeGymApp\\Models\\' => $base . 'models/',
        'CodeGymApp\\Helpers\\' => $base . 'helpers/',
    ];

    foreach ($prefixes as $prefix => $directory) {
        if (!str_starts_with($class, $prefix)) {
            continue;
        }

        $relativeClass = substr($class, strlen($prefix));
        $file = $directory . str_replace('\\', '/', $relativeClass) . '.php';
        if (is_file($file)) {
            require_once $file;
        }

        return;
    }

    foreach (['core', 'controllers', 'models', 'helpers'] as $folder) {
        $file = $base . $folder . '/' . $class . '.php';
        if (is_file($file)) {
            require_once $file;
            return;
        }
    }
});

(new Application(dirname(__DIR__, 2)))->bootstrap();
