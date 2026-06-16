<?php

declare(strict_types=1);

use CodeGymApp\Services\ChallengeService;

final class ChallengeController
{
    public function __construct(private readonly ChallengeService $challengeService = new ChallengeService())
    {
    }

    public function index(): void
    {
        $data = $this->challengeService->indexPayload($_GET);
        if (($_SERVER['HTTP_HX_REQUEST'] ?? '') === 'true') {
            View::render('challenges/table', $data);
            return;
        }
        View::render('challenges/index', $data, 'main');
    }

    public function manual(): void
    {
        verify_csrf();

        $result = $this->challengeService->createManual($_POST);
        if (!$result['ok']) {
            $_SESSION['flash_error'] = implode(' ', $result['errors']);
            Response::redirect('/retos');
        }

        $_SESSION['flash_success'] = 'Reto manual registrado correctamente.';
        Response::redirect('/retos?status=completed');
    }
}
