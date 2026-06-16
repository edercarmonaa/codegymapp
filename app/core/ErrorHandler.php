<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

use CodeGymApp\Core\Exceptions\ConfigurationException;

final class ErrorHandler
{
    private function __construct()
    {
    }

    public static function register(bool $debug): void
    {
        ini_set('display_errors', $debug ? '1' : '0');
        ini_set('log_errors', '1');
        error_reporting(E_ALL);

        set_exception_handler(static function (\Throwable $exception) use ($debug): void {
            self::renderException($exception, $debug);
        });
    }

    private static function renderException(\Throwable $exception, bool $debug): void
    {
        error_log((string) $exception);

        if (!headers_sent()) {
            http_response_code(500);
            header('Content-Type: text/html; charset=utf-8');
        }

        if ($debug) {
            echo '<pre>' . htmlspecialchars((string) $exception, ENT_QUOTES, 'UTF-8') . '</pre>';
            return;
        }

        if ($exception instanceof ConfigurationException) {
            echo 'Configuracion incompleta. Revisa el archivo .env.';
            return;
        }

        echo 'Error interno del servidor.';
    }
}

if (!\class_exists('ErrorHandler', false)) {
    \class_alias(ErrorHandler::class, 'ErrorHandler');
}
