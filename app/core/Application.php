<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class Application
{
    public function __construct(private readonly string $rootPath)
    {
    }

    public function bootstrap(): void
    {
        require_once $this->rootPath . '/app/helpers/Security.php';

        Env::load($this->rootPath . '/.env');
        ErrorHandler::register((bool) Env::get('APP_DEBUG', false));
        RateLimiter::enforce();
        Config::validate();

        date_default_timezone_set('America/Mexico_City');
        SessionManager::start('codegymapp_flash');
    }

    public function run(Router $router, string $method, string $path): void
    {
        $router->dispatch($method, $path);
    }
}

if (!\class_exists('Application', false)) {
    \class_alias(Application::class, 'Application');
}
