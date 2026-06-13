<?php

declare(strict_types=1);

final class ChallengeController
{
    public function index(): void
    {
        Challenge::expirePending();
        $filters = [
            'status' => (string) ($_GET['status'] ?? ''),
            'platform_id' => (int) ($_GET['platform_id'] ?? 0),
        ];

        View::render('challenges/index', [
            'title' => 'Retos',
            'challenges' => Challenge::allForList($filters),
            'platforms' => Platform::all(),
            'activePlatforms' => Platform::active(),
            'activeLanguages' => Language::active(),
            'statusLabels' => Challenge::statusLabels(),
            'statusBadgeClasses' => Challenge::statusBadgeClasses(),
            'filters' => $filters,
        ], 'main');
    }

    public function manual(): void
    {
        verify_csrf();

        $errors = [];
        $title = trim((string) ($_POST['title'] ?? ''));
        $difficulty = trim((string) ($_POST['difficulty'] ?? ''));
        $timeSpent = (int) ($_POST['time_spent_minutes'] ?? 0);
        $platformId = (int) ($_POST['platform_id'] ?? 0);
        $languageIds = $_POST['language_ids'] ?? [];

        if ($platformId <= 0 || !Platform::existsActive($platformId)) {
            $errors[] = 'Selecciona una plataforma activa.';
        }
        if ($title === '') {
            $errors[] = 'Captura el nombre del reto.';
        }
        if ($difficulty === '') {
            $errors[] = 'Captura la dificultad.';
        }
        if ($timeSpent <= 0) {
            $errors[] = 'Captura el tiempo invertido.';
        }
        if (!is_array($languageIds) || count(array_filter($languageIds)) === 0) {
            $errors[] = 'Selecciona al menos un lenguaje.';
        }

        if ($errors) {
            $_SESSION['flash_error'] = implode(' ', $errors);
            Response::redirect('/retos');
        }

        Challenge::createManual([
            'platform_id' => $platformId,
            'title' => $title,
            'challenge_url' => (string) ($_POST['challenge_url'] ?? ''),
            'difficulty' => $difficulty,
            'time_spent_minutes' => $timeSpent,
            'notes' => (string) ($_POST['notes'] ?? ''),
            'language_ids' => $languageIds,
            'github_links' => (string) ($_POST['github_links'] ?? ''),
        ]);

        $_SESSION['flash_success'] = 'Reto manual registrado correctamente.';
        Response::redirect('/retos?status=completed');
    }
}
