<?php

declare(strict_types=1);

final class PlatformController
{
    public function index(): void
    {
        $state = TableState::fromRequest(['name', 'is_active', 'created_at'], 'name', 'asc');
        $data = [
            'title' => 'Plataformas',
            'platforms' => Platform::paginated($state),
            'pagination' => TableState::pagination($state, Platform::countAll()),
        ];
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('platforms/table', $data);
            return;
        }
        View::render('platforms/index', $data, 'main');
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
