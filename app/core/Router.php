<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

use CodeGymApp\Core\Exceptions\RouteNotFoundException;

final class Router
{
    /** @var array<string, array<string, array{0: string, 1: string, 2: bool}>> */
    private array $routes = [];

    public function get(string $path, string $controller, string $action, bool $private = true): void
    {
        $this->add('GET', $path, $controller, $action, $private);
    }

    public function post(string $path, string $controller, string $action, bool $private = true): void
    {
        $this->add('POST', $path, $controller, $action, $private);
    }

    private function add(string $method, string $path, string $controller, string $action, bool $private): void
    {
        $this->routes[$method][$this->normalize($path)] = [$controller, $action, $private];
    }

    public function dispatch(string $method, string $path): void
    {
        $path = $this->normalize($path);
        $route = $this->routes[$method][$path] ?? null;

        if ($route === null) {
            http_response_code(404);
            if (str_starts_with($path, '/api/')) {
                Response::json(['ok' => false, 'message' => 'Ruta no encontrada.']);
                return;
            }

            View::render('errors/not_found', ['title' => 'No encontrado'], 'main');
            return;
        }

        [$controllerName, $action, $private] = $route;
        if ($private && !Auth::check()) {
            \SecurityLog::record(null, 'unauthorized_access', 'failure', 'Acceso no autorizado a ' . $path);
            if (str_starts_with($path, '/api/')) {
                http_response_code(401);
                Response::json(['ok' => false, 'message' => 'No autenticado.']);
                return;
            }

            Response::redirect('/login');
        }

        if (!\class_exists($controllerName) || !\method_exists($controllerName, $action)) {
            throw new RouteNotFoundException('Controlador o accion no encontrada: ' . $controllerName . '::' . $action);
        }

        $controller = new $controllerName();
        $controller->$action();
    }

    private function normalize(string $path): string
    {
        $path = '/' . trim($path, '/');
        return $path === '/' ? '/' : rtrim($path, '/');
    }
}

if (!\class_exists('Router', false)) {
    \class_alias(Router::class, 'Router');
}
