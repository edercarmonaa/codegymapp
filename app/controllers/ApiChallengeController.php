<?php

declare(strict_types=1);

use CodeGymApp\Services\ChallengeService;

final class ApiChallengeController
{
    public function __construct(private readonly ChallengeService $challengeService = new ChallengeService())
    {
    }

    public function manual(): void
    {
        verify_csrf();
        $result = $this->challengeService->createManual($_POST);
        if (!$result['ok']) {
            http_response_code(422);
            Response::json([
                'ok' => false,
                'message' => implode(' ', $result['errors']),
            ]);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto manual registrado correctamente.']);
    }
}
