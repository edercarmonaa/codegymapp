<?php

declare(strict_types=1);

spl_autoload_register(static function (string $class): void {
    $base = __DIR__ . '/../';
    foreach (['core', 'controllers', 'models', 'helpers'] as $folder) {
        $file = $base . $folder . '/' . $class . '.php';
        if (is_file($file)) {
            require_once $file;
            return;
        }
    }
});

require_once __DIR__ . '/../helpers/Security.php';

Env::load(dirname(__DIR__, 2) . '/.env');

if ((bool) Env::get('APP_DEBUG', false)) {
    ini_set('display_errors', '1');
    error_reporting(E_ALL);
}

Config::validate();

date_default_timezone_set('America/Mexico_City');

session_name('codegymapp_flash');
session_start();
