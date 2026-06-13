<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

final class View
{
    /** @param array<string, mixed> $data */
    public static function render(string $view, array $data = [], string $layout = ''): void
    {
        extract($data, EXTR_SKIP);
        $viewFile = __DIR__ . '/../views/' . $view . '.php';

        if ($layout === '') {
            require $viewFile;
            return;
        }

        ob_start();
        require $viewFile;
        $content = ob_get_clean();
        require __DIR__ . '/../views/layouts/' . $layout . '.php';
    }

    /** @param array<string, mixed> $data */
    public static function partial(string $view, array $data = []): string
    {
        extract($data, EXTR_SKIP);
        ob_start();
        require __DIR__ . '/../views/' . $view . '.php';
        return (string) ob_get_clean();
    }
}

if (!\class_exists('View', false)) {
    \class_alias(View::class, 'View');
}
