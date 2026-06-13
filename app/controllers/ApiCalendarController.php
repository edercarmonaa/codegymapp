<?php

declare(strict_types=1);

final class ApiCalendarController
{
    public function events(): void
    {
        Routine::generateCurrentMonth();
        Challenge::expirePending();
        Response::json(Challenge::calendarEvents(
            isset($_GET['start']) ? substr((string) $_GET['start'], 0, 10) : null,
            isset($_GET['end']) ? substr((string) $_GET['end'], 0, 10) : null
        ));
    }

    public function challenge(): void
    {
        $id = (int) ($_GET['id'] ?? 0);
        $challenge = $id > 0 ? Challenge::detail($id) : null;
        if (!$challenge) {
            http_response_code(404);
            Response::json(['ok' => false, 'message' => 'Reto no encontrado.']);
            return;
        }

        Response::json(['ok' => true, 'challenge' => $challenge]);
    }

    public function store(): void
    {
        verify_csrf();

        $platformId = (int) ($_POST['platform_id'] ?? 0);
        $scheduledDate = substr((string) ($_POST['scheduled_date'] ?? ''), 0, 10);

        if ($platformId <= 0 || !$this->isDate($scheduledDate) || !Platform::existsActive($platformId)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Selecciona una plataforma y una fecha válida.']);
            return;
        }

        $id = Challenge::createFromCalendar($platformId, $scheduledDate);
        Response::json(['ok' => true, 'id' => $id, 'message' => 'Reto guardado correctamente.']);
    }

    public function updateDate(): void
    {
        verify_csrf();

        $payload = json_decode(file_get_contents('php://input') ?: '{}', true);
        if (!is_array($payload)) {
            $payload = [];
        }

        $id = (int) ($payload['id'] ?? 0);
        $scheduledDate = substr((string) ($payload['scheduled_date'] ?? ''), 0, 10);

        if ($id <= 0 || !$this->isDate($scheduledDate)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo reprogramar el reto.']);
            return;
        }

        if (!Challenge::updateScheduledDate($id, $scheduledDate)) {
            http_response_code(409);
            Response::json(['ok' => false, 'message' => 'Este reto no se puede mover.']);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto reprogramado.']);
    }

    public function saveDetails(): void
    {
        verify_csrf();

        $id = (int) ($_POST['id'] ?? 0);
        if ($id <= 0) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'No se pudo identificar el reto.']);
            return;
        }

        $saved = Challenge::saveDetails($id, [
            'title' => $_POST['title'] ?? '',
            'challenge_url' => $_POST['challenge_url'] ?? '',
            'difficulty' => $_POST['difficulty'] ?? '',
            'time_spent_minutes' => $_POST['time_spent_minutes'] ?? null,
            'notes' => $_POST['notes'] ?? '',
            'language_ids' => $_POST['language_ids'] ?? [],
            'github_links' => $_POST['github_links'] ?? '',
        ]);

        if (!$saved) {
            http_response_code(409);
            Response::json(['ok' => false, 'message' => 'Este reto ya no se puede editar.']);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto actualizado correctamente.']);
    }

    public function complete(): void
    {
        verify_csrf();
        $id = (int) ($_POST['id'] ?? 0);
        $errors = Challenge::completionErrors($id);
        if ($errors) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => implode(' ', $errors)]);
            return;
        }

        if (!Challenge::complete($id)) {
            http_response_code(409);
            Response::json(['ok' => false, 'message' => 'Este reto no se puede marcar como cumplido.']);
            return;
        }

        Response::json(['ok' => true, 'message' => 'Reto marcado como cumplido.']);
    }

    public function miss(): void
    {
        $this->closeWith(fn (int $id): bool => Challenge::miss($id), 'Reto marcado como no cumplido.', 'Este reto no se puede marcar como no cumplido.');
    }

    public function cancel(): void
    {
        $this->closeWith(fn (int $id): bool => Challenge::cancel($id), 'Reto cancelado.', 'Este reto no se puede cancelar.');
    }

    public function storeRoutine(): void
    {
        verify_csrf();

        $platformId = (int) ($_POST['platform_id'] ?? 0);
        $frequency = (string) ($_POST['frequency_type'] ?? '');
        $startDate = substr((string) ($_POST['start_date'] ?? ''), 0, 10);
        $endDate = substr((string) ($_POST['end_date'] ?? ''), 0, 10);
        $monthDay = (int) ($_POST['month_day'] ?? 0);
        $weekDays = $_POST['week_days'] ?? [];

        if ($platformId <= 0 || !Platform::existsActive($platformId) || !in_array($frequency, ['daily', 'weekly', 'monthly'], true) || !$this->isDate($startDate)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Completa plataforma, frecuencia y fecha de inicio.']);
            return;
        }
        if ($endDate !== '' && !$this->isDate($endDate)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'La fecha final no es válida.']);
            return;
        }
        if ($endDate !== '' && $endDate < $startDate) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'La fecha final no puede ser anterior al inicio.']);
            return;
        }
        if ($frequency === 'weekly' && (!is_array($weekDays) || count(array_filter($weekDays)) === 0)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Selecciona al menos un día de la semana.']);
            return;
        }
        if ($frequency === 'monthly' && ($monthDay < 1 || $monthDay > 31)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Captura un día del mes válido.']);
            return;
        }

        Routine::create([
            'platform_id' => $platformId,
            'frequency_type' => $frequency,
            'week_days' => is_array($weekDays) ? $weekDays : [],
            'month_day' => $monthDay,
            'start_date' => $startDate,
            'end_date' => $endDate,
        ]);
        Routine::generateCurrentMonth();

        Response::json(['ok' => true, 'message' => 'Rutina creada correctamente.']);
    }

    public function updateRoutine(): void
    {
        verify_csrf();

        $id = (int) ($_POST['id'] ?? 0);
        $platformId = (int) ($_POST['platform_id'] ?? 0);
        $frequency = (string) ($_POST['frequency_type'] ?? '');
        $startDate = substr((string) ($_POST['start_date'] ?? ''), 0, 10);
        $endDate = substr((string) ($_POST['end_date'] ?? ''), 0, 10);
        $monthDay = (int) ($_POST['month_day'] ?? 0);
        $weekDays = $_POST['week_days'] ?? [];

        if ($id <= 0 || $platformId <= 0 || !Platform::existsActive($platformId) || !in_array($frequency, ['daily', 'weekly', 'monthly'], true) || !$this->isDate($startDate)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Completa plataforma, frecuencia y fecha de inicio.']);
            return;
        }
        if ($endDate !== '' && !$this->isDate($endDate)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'La fecha final no es válida.']);
            return;
        }
        if ($endDate !== '' && $endDate < $startDate) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'La fecha final no puede ser anterior al inicio.']);
            return;
        }
        if ($frequency === 'weekly' && (!is_array($weekDays) || count(array_filter($weekDays)) === 0)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Selecciona al menos un día de la semana.']);
            return;
        }
        if ($frequency === 'monthly' && ($monthDay < 1 || $monthDay > 31)) {
            http_response_code(422);
            Response::json(['ok' => false, 'message' => 'Captura un día del mes válido.']);
            return;
        }

        Routine::update($id, [
            'platform_id' => $platformId,
            'frequency_type' => $frequency,
            'week_days' => is_array($weekDays) ? $weekDays : [],
            'month_day' => $monthDay,
            'start_date' => $startDate,
            'end_date' => $endDate,
        ]);

        Response::json(['ok' => true, 'message' => 'Rutina actualizada correctamente.']);
    }

    public function disableRoutine(): void
    {
        verify_csrf();
        Routine::disable((int) ($_POST['id'] ?? 0));
        Response::json(['ok' => true, 'message' => 'Rutina desactivada.']);
    }

    private function isDate(string $date): bool
    {
        $parsed = DateTimeImmutable::createFromFormat('Y-m-d', $date);
        return $parsed instanceof DateTimeImmutable && $parsed->format('Y-m-d') === $date;
    }

    /** @param callable(int): bool $action */
    private function closeWith(callable $action, string $success, string $failure): void
    {
        verify_csrf();
        $id = (int) ($_POST['id'] ?? 0);
        if ($id <= 0 || !$action($id)) {
            http_response_code(409);
            Response::json(['ok' => false, 'message' => $failure]);
            return;
        }

        Response::json(['ok' => true, 'message' => $success]);
    }
}
