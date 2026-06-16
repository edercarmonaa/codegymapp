<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

use CodeGymApp\Core\Exceptions\ViewNotFoundException;

final class View
{
    /** @param array<string, mixed> $data */
    public static function render(string $view, array $data = [], string $layout = ''): void
    {
        extract($data, EXTR_SKIP);
        $viewFile = __DIR__ . '/../views/' . $view . '.php';
        if (!is_file($viewFile)) {
            throw new ViewNotFoundException('Vista no encontrada: ' . $view);
        }

        if ($layout === '') {
            require $viewFile;
            return;
        }

        ob_start();
        require $viewFile;
        $content = ob_get_clean();
        $layoutFile = __DIR__ . '/../views/layouts/' . $layout . '.php';
        if (!is_file($layoutFile)) {
            throw new ViewNotFoundException('Layout no encontrado: ' . $layout);
        }
        require $layoutFile;
    }

    /** @param array<string, mixed> $data */
    public static function partial(string $view, array $data = []): string
    {
        extract($data, EXTR_SKIP);
        ob_start();
        $viewFile = __DIR__ . '/../views/' . $view . '.php';
        if (!is_file($viewFile)) {
            throw new ViewNotFoundException('Partial no encontrado: ' . $view);
        }
        require $viewFile;
        return (string) ob_get_clean();
    }
}

if (!\class_exists('View', false)) {
    \class_alias(View::class, 'View');
}
