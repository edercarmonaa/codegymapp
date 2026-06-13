<?php

declare(strict_types=1);

final class LanguageController
{
    public function index(): void
    {
        $state = TableState::fromRequest(['name', 'is_active', 'created_at'], 'name', 'asc');
        View::render('languages/index', [
            'title' => 'Lenguajes',
            'languages' => Language::paginated($state),
            'pagination' => TableState::pagination($state, Language::countAll()),
        ], 'main');
    }

    public function save(): void
    {
        verify_csrf();
        Language::save(['id' => (string) ($_POST['id'] ?? ''), 'name' => trim((string) ($_POST['name'] ?? ''))]);
        Response::redirect('/lenguajes');
    }

    public function deactivate(): void
    {
        verify_csrf();
        Language::setActive((int) ($_POST['id'] ?? 0), false);
        Response::redirect('/lenguajes');
    }

    public function activate(): void
    {
        verify_csrf();
        Language::setActive((int) ($_POST['id'] ?? 0), true);
        Response::redirect('/lenguajes');
    }
}
