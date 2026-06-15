<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class RoutineRequestValidator
{
    private const FREQUENCIES = ['daily', 'weekly', 'monthly'];

    public function __construct(private readonly DateValidator $dateValidator = new DateValidator())
    {
    }

    /**
     * @param array<string, mixed> $input
     * @return array{ok: bool, message?: string, data?: array<string, mixed>}
     */
    public function validate(array $input, bool $requireId): array
    {
        $id = (int) ($input['id'] ?? 0);
        $platformId = (int) ($input['platform_id'] ?? 0);
        $frequency = (string) ($input['frequency_type'] ?? '');
        $startDate = substr((string) ($input['start_date'] ?? ''), 0, 10);
        $endDate = substr((string) ($input['end_date'] ?? ''), 0, 10);
        $monthDay = (int) ($input['month_day'] ?? 0);
        $weekDays = $input['week_days'] ?? [];

        if (
            ($requireId && $id <= 0)
            || $platformId <= 0
            || !\Platform::existsActive($platformId)
            || !in_array($frequency, self::FREQUENCIES, true)
            || !$this->dateValidator->isDate($startDate)
        ) {
            return ['ok' => false, 'message' => 'Completa plataforma, frecuencia y fecha de inicio.'];
        }

        if ($endDate !== '' && !$this->dateValidator->isDate($endDate)) {
            return ['ok' => false, 'message' => 'La fecha final no es válida.'];
        }

        if ($endDate !== '' && $endDate < $startDate) {
            return ['ok' => false, 'message' => 'La fecha final no puede ser anterior al inicio.'];
        }

        if ($frequency === 'weekly' && (!is_array($weekDays) || count(array_filter($weekDays)) === 0)) {
            return ['ok' => false, 'message' => 'Selecciona al menos un día de la semana.'];
        }

        if ($frequency === 'monthly' && ($monthDay < 1 || $monthDay > 31)) {
            return ['ok' => false, 'message' => 'Captura un día del mes válido.'];
        }

        return [
            'ok' => true,
            'data' => [
                'id' => $id,
                'platform_id' => $platformId,
                'frequency_type' => $frequency,
                'week_days' => is_array($weekDays) ? $weekDays : [],
                'month_day' => $monthDay,
                'start_date' => $startDate,
                'end_date' => $endDate,
            ],
        ];
    }
}
