<?php

declare(strict_types=1);

final class GoalController
{
    public function index(): void
    {
        Goal::refreshActiveProgress();
        $state = TableState::fromRequest(['goal_type', 'period_end', 'progress_percent', 'status'], 'period_end', 'asc');
        $data = [
            'title' => 'Metas',
            'goals' => Goal::paginated($state),
            'pagination' => TableState::pagination($state, Goal::countAll()),
            'platforms' => Platform::active(),
            'languages' => Language::active(),
            'goalTypes' => Goal::goalTypes(),
            'periodTypes' => Goal::periodTypes(),
        ];
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('goals/table', $data);
            return;
        }
        View::render('goals/index', $data, 'main');
    }

    public function save(): void
    {
        verify_csrf();

        $target = (int) ($_POST['target_value'] ?? 0);
        if ($target <= 0) {
            $_SESSION['flash_error'] = 'Captura un objetivo mayor a cero.';
            Response::redirect('/metas');
        }

        Goal::create([
            'goal_type' => (string) ($_POST['goal_type'] ?? ''),
            'period_type' => (string) ($_POST['period_type'] ?? ''),
            'target_value' => $target,
            'platform_id' => (int) ($_POST['platform_id'] ?? 0),
            'language_id' => (int) ($_POST['language_id'] ?? 0),
            'auto_renew' => isset($_POST['auto_renew']) ? 1 : 0,
        ]);

        $_SESSION['flash_success'] = 'Meta creada correctamente.';
        Response::redirect('/metas');
    }

    public function deactivate(): void
    {
        verify_csrf();
        Goal::close((int) ($_POST['id'] ?? 0));
        $_SESSION['flash_success'] = 'Meta desactivada.';
        Response::redirect('/metas');
    }
}
