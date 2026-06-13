<?php

declare(strict_types=1);

final class ChallengeController
{
    public function index(): void
    {
        Challenge::expirePending();
        View::render('challenges/index', ['title' => 'Retos', 'challenges' => Challenge::allForList()], 'main');
    }
}

