<?php

declare(strict_types=1);

final class GoalController
{
    public function index(): void
    {
        View::render('goals/index', ['title' => 'Metas'], 'main');
    }
}

