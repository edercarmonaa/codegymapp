<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class CalendarService
{
    public function __construct(
        private readonly DateValidator $dateValidator = new DateValidator(),
        private readonly RoutineRequestValidator $routineValidator = new RoutineRequestValidator()
    ) {
    }

    public function refreshCalendar(): void
    {
        \Routine::generateCurrentMonth();
        \Challenge::expirePending();
    }

    /** @return array{status: int, payload: array<string, mixed>} */
    public function bootstrapData(): array
    {
        $this->refreshCalendar();

        return $this->response(200, [
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], \Platform::all()),
            'languages' => array_map([$this, 'languageResource'], \Language::all()),
            'routines' => array_map([$this, 'routineResource'], \Routine::allForList()),
        ]);
    }

    /** @return array{status: int, payload: array<string, mixed>} */
    public function routines(): array
    {
        $this->refreshCalendar();

        return $this->response(200, [
            'ok' => true,
            'routines' => array_map([$this, 'routineResource'], \Routine::allForList()),
        ]);
    }

    /**
     * @param array<string, mixed> $query
     * @return array<int, array<string, mixed>>
     */
    public function events(array $query): array
    {
        $this->refreshCalendar();

        return \Challenge::calendarEvents(
            isset($query['start']) ? substr((string) $query['start'], 0, 10) : null,
            isset($query['end']) ? substr((string) $query['end'], 0, 10) : null
        );
    }

    /** @return array{status: int, payload: array<string, mixed>} */
    public function challengeDetail(int $id): array
    {
        $challenge = $id > 0 ? \Challenge::detail($id) : null;
        if (!$challenge) {
            return $this->response(404, ['ok' => false, 'message' => 'Reto no encontrado.']);
        }

        return $this->response(200, ['ok' => true, 'challenge' => $challenge]);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function createChallenge(array $input): array
    {
        $platformId = (int) ($input['platform_id'] ?? 0);
        $scheduledDate = substr((string) ($input['scheduled_date'] ?? ''), 0, 10);

        if ($platformId <= 0 || !$this->dateValidator->isDate($scheduledDate) || !\Platform::existsActive($platformId)) {
            return $this->response(422, ['ok' => false, 'message' => 'Selecciona una plataforma y una fecha válida.']);
        }

        $id = \Challenge::createFromCalendar($platformId, $scheduledDate);
        return $this->response(200, ['ok' => true, 'id' => $id, 'message' => 'Reto guardado correctamente.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function updateChallengeDate(array $input): array
    {
        $id = (int) ($input['id'] ?? 0);
        $scheduledDate = substr((string) ($input['scheduled_date'] ?? ''), 0, 10);

        if ($id <= 0 || !$this->dateValidator->isDate($scheduledDate)) {
            return $this->response(422, ['ok' => false, 'message' => 'No se pudo reprogramar el reto.']);
        }

        if (!\Challenge::updateScheduledDate($id, $scheduledDate)) {
            return $this->response(409, ['ok' => false, 'message' => 'Este reto no se puede mover.']);
        }

        return $this->response(200, ['ok' => true, 'message' => 'Reto reprogramado.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function saveChallengeDetails(array $input): array
    {
        $id = (int) ($input['id'] ?? 0);
        if ($id <= 0) {
            return $this->response(422, ['ok' => false, 'message' => 'No se pudo identificar el reto.']);
        }

        $saved = \Challenge::saveDetails($id, [
            'title' => $input['title'] ?? '',
            'challenge_url' => $input['challenge_url'] ?? '',
            'difficulty' => $input['difficulty'] ?? '',
            'time_spent_minutes' => $input['time_spent_minutes'] ?? null,
            'notes' => $input['notes'] ?? '',
            'language_ids' => $input['language_ids'] ?? [],
            'github_links' => $input['github_links'] ?? '',
        ]);

        if (!$saved) {
            return $this->response(409, ['ok' => false, 'message' => 'Este reto ya no se puede editar.']);
        }

        return $this->response(200, ['ok' => true, 'message' => 'Reto actualizado correctamente.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function completeChallenge(array $input): array
    {
        $id = (int) ($input['id'] ?? 0);
        $errors = \Challenge::completionErrors($id);
        if ($errors) {
            return $this->response(422, ['ok' => false, 'message' => implode(' ', $errors)]);
        }

        if (!\Challenge::complete($id)) {
            return $this->response(409, ['ok' => false, 'message' => 'Este reto no se puede marcar como cumplido.']);
        }

        return $this->response(200, ['ok' => true, 'message' => 'Reto marcado como cumplido.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function missChallenge(array $input): array
    {
        return $this->closeChallenge(
            $input,
            static fn (int $id): bool => \Challenge::miss($id),
            'Reto marcado como no cumplido.',
            'Este reto no se puede marcar como no cumplido.'
        );
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function cancelChallenge(array $input): array
    {
        return $this->closeChallenge(
            $input,
            static fn (int $id): bool => \Challenge::cancel($id),
            'Reto cancelado.',
            'Este reto no se puede cancelar.'
        );
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function createRoutine(array $input): array
    {
        $validated = $this->routineValidator->validate($input, false);
        if (!$validated['ok']) {
            return $this->response(422, ['ok' => false, 'message' => (string) $validated['message']]);
        }

        \Routine::create($validated['data']);
        \Routine::generateCurrentMonth();

        return $this->response(200, ['ok' => true, 'message' => 'Rutina creada correctamente.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function updateRoutine(array $input): array
    {
        $validated = $this->routineValidator->validate($input, true);
        if (!$validated['ok']) {
            return $this->response(422, ['ok' => false, 'message' => (string) $validated['message']]);
        }

        $data = $validated['data'];
        \Routine::update((int) $data['id'], $data);

        return $this->response(200, ['ok' => true, 'message' => 'Rutina actualizada correctamente.']);
    }

    /** @param array<string, mixed> $input @return array{status: int, payload: array<string, mixed>} */
    public function disableRoutine(array $input): array
    {
        \Routine::disable((int) ($input['id'] ?? 0));
        return $this->response(200, ['ok' => true, 'message' => 'Rutina desactivada.']);
    }

    /**
     * @param array<string, mixed> $input
     * @param callable(int): bool $action
     * @return array{status: int, payload: array<string, mixed>}
     */
    private function closeChallenge(array $input, callable $action, string $success, string $failure): array
    {
        $id = (int) ($input['id'] ?? 0);
        if ($id <= 0 || !$action($id)) {
            return $this->response(409, ['ok' => false, 'message' => $failure]);
        }

        return $this->response(200, ['ok' => true, 'message' => $success]);
    }

    /** @param array<string, mixed> $platform @return array<string, mixed> */
    private function platformResource(array $platform): array
    {
        return [
            'id' => (int) ($platform['id'] ?? 0),
            'name' => (string) ($platform['name'] ?? ''),
            'description' => (string) ($platform['description'] ?? ''),
            'url' => safe_url($platform['url'] ?? null),
            'is_active' => (int) ($platform['is_active'] ?? 0) === 1,
        ];
    }

    /** @param array<string, mixed> $language @return array<string, mixed> */
    private function languageResource(array $language): array
    {
        return [
            'id' => (int) ($language['id'] ?? 0),
            'name' => (string) ($language['name'] ?? ''),
            'is_active' => (int) ($language['is_active'] ?? 0) === 1,
        ];
    }

    /** @param array<string, mixed> $routine @return array<string, mixed> */
    private function routineResource(array $routine): array
    {
        return [
            'id' => (int) ($routine['id'] ?? 0),
            'platform_id' => (int) ($routine['platform_id'] ?? 0),
            'platform_name' => (string) ($routine['platform_name'] ?? ''),
            'frequency_type' => (string) ($routine['frequency_type'] ?? ''),
            'week_days' => (string) ($routine['week_days'] ?? ''),
            'month_day' => isset($routine['month_day']) ? (int) $routine['month_day'] : null,
            'start_date' => (string) ($routine['start_date'] ?? ''),
            'end_date' => $routine['end_date'] ?? null,
            'is_active' => (int) ($routine['is_active'] ?? 0) === 1,
            'created_at' => (string) ($routine['created_at'] ?? ''),
        ];
    }

    /** @param array<string, mixed> $payload @return array{status: int, payload: array<string, mixed>} */
    private function response(int $status, array $payload): array
    {
        return ['status' => $status, 'payload' => $payload];
    }
}
