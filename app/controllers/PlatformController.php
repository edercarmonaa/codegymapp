<?php

declare(strict_types=1);

final class PlatformController
{
    public function index(): void
    {
        $state = TableState::fromRequest(['name', 'is_active', 'created_at'], 'name', 'asc');
        View::render('platforms/index', [
            'title' => 'Plataformas',
            'platforms' => Platform::paginated($state),
            'pagination' => TableState::pagination($state, Platform::countAll()),
        ], 'main');
    }

    public function save(): void
    {
        verify_csrf();
        Platform::save([
            'id' => (string) ($_POST['id'] ?? ''),
            'name' => trim((string) ($_POST['name'] ?? '')),
            'description' => trim((string) ($_POST['description'] ?? '')),
            'url' => trim((string) ($_POST['url'] ?? '')),
        ]);
        Response::redirect('/plataformas');
    }

    public function deactivate(): void
    {
        verify_csrf();
        Platform::setActive((int) ($_POST['id'] ?? 0), false);
        Response::redirect('/plataformas');
    }

    public function activate(): void
    {
        verify_csrf();
        Platform::setActive((int) ($_POST['id'] ?? 0), true);
        Response::redirect('/plataformas');
    }
}
