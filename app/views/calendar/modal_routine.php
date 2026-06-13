<?php $weekDayLabels = [1 => 'Lun', 2 => 'Mar', 3 => 'Mié', 4 => 'Jue', 5 => 'Vie', 6 => 'Sáb', 7 => 'Dom']; ?>
<div class="modal fade" id="routineModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5">Rutinas repetitivas</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="routineAlert" class="alert alert-danger d-none"></div>
                <form id="routineForm" class="border rounded-2 p-3 mb-4">
                    <?= csrf_field() ?>
                    <input id="routineId" name="id" type="hidden">
                    <div class="row g-3">
                        <div class="col-12 col-md-4">
                            <label class="form-label" for="routinePlatform">Plataforma</label>
                            <select class="form-select" id="routinePlatform" name="platform_id" required>
                                <option value="">Selecciona una plataforma</option>
                                <?php foreach ($platforms as $platform): ?>
                                    <?php if ((int) $platform['is_active'] === 1): ?>
                                        <option value="<?= e((string) $platform['id']) ?>"><?= e($platform['name']) ?></option>
                                    <?php endif; ?>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label" for="routineFrequency">Frecuencia</label>
                            <select class="form-select" id="routineFrequency" name="frequency_type" required>
                                <option value="daily">Diaria</option>
                                <option value="weekly">Semanal</option>
                                <option value="monthly">Mensual</option>
                            </select>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label" for="routineStartDate">Fecha de inicio</label>
                            <input class="form-control" id="routineStartDate" name="start_date" type="date" required>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label" for="routineEndDate">Fecha final</label>
                            <input class="form-control" id="routineEndDate" name="end_date" type="date">
                        </div>
                        <div class="col-12 col-md-8 routine-weekly">
                            <label class="form-label">Días de semana</label>
                            <div class="d-flex flex-wrap gap-3">
                                <?php foreach ($weekDayLabels as $value => $label): ?>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="week_days[]" value="<?= e((string) $value) ?>" id="routineWeekDay<?= e((string) $value) ?>">
                                        <label class="form-check-label" for="routineWeekDay<?= e((string) $value) ?>"><?= e($label) ?></label>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        </div>
                        <div class="col-12 col-md-4 routine-monthly d-none">
                            <label class="form-label" for="routineMonthDay">Día del mes</label>
                            <input class="form-control" id="routineMonthDay" name="month_day" type="number" min="1" max="31">
                        </div>
                        <div class="col-12">
                            <button class="btn btn-primary" id="routineSubmitButton">Crear rutina</button>
                            <button class="btn btn-outline-secondary d-none" id="routineResetButton" type="button">Nueva rutina</button>
                        </div>
                    </div>
                </form>

                <h3 class="h6">Rutinas registradas</h3>
                <div class="table-responsive">
                    <table class="table table-sm align-middle">
                        <thead>
                            <tr><th>Plataforma</th><th>Frecuencia</th><th>Periodo</th><th>Estado</th><th class="text-end">Acciones</th></tr>
                        </thead>
                        <tbody>
                            <?php foreach ($routines as $routine): ?>
                                <?php
                                $selectedWeekDays = array_filter(array_map('intval', explode(',', (string) $routine['week_days'])));
                                $selectedWeekDayLabels = array_map(fn (int $day): string => $weekDayLabels[$day] ?? (string) $day, $selectedWeekDays);
                                ?>
                                <tr>
                                    <td><?= e($routine['platform_name']) ?></td>
                                    <td>
                                        <?= e(match ($routine['frequency_type']) {
                                            'daily' => 'Diaria',
                                            'weekly' => 'Semanal',
                                            'monthly' => 'Mensual',
                                            default => $routine['frequency_type'],
                                        }) ?>
                                        <?php if ($routine['frequency_type'] === 'weekly'): ?>
                                            <span class="text-body-secondary">(<?= e(implode(', ', $selectedWeekDayLabels)) ?>)</span>
                                        <?php endif; ?>
                                        <?php if ($routine['frequency_type'] === 'monthly'): ?>
                                            <span class="text-body-secondary">(día <?= e((string) $routine['month_day']) ?>)</span>
                                        <?php endif; ?>
                                    </td>
                                    <td><?= e($routine['start_date']) ?> a <?= e($routine['end_date'] ?: 'sin final') ?></td>
                                    <td><span class="badge <?= (int) $routine['is_active'] === 1 ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= (int) $routine['is_active'] === 1 ? 'Activa' : 'Inactiva' ?></span></td>
                                    <td class="text-end">
                                        <?php if ((int) $routine['is_active'] === 1): ?>
                                            <div class="d-flex gap-2 justify-content-end">
                                                <button
                                                    class="btn btn-sm btn-outline-primary routine-edit-button"
                                                    type="button"
                                                    data-id="<?= e((string) $routine['id']) ?>"
                                                    data-platform-id="<?= e((string) $routine['platform_id']) ?>"
                                                    data-frequency-type="<?= e((string) $routine['frequency_type']) ?>"
                                                    data-week-days="<?= e((string) $routine['week_days']) ?>"
                                                    data-month-day="<?= e((string) $routine['month_day']) ?>"
                                                    data-start-date="<?= e((string) $routine['start_date']) ?>"
                                                    data-end-date="<?= e((string) ($routine['end_date'] ?? '')) ?>"
                                                >Editar</button>
                                                <button class="btn btn-sm btn-outline-secondary routine-disable-button" data-id="<?= e((string) $routine['id']) ?>">Desactivar</button>
                                            </div>
                                        <?php else: ?>
                                            <span class="text-body-secondary">-</span>
                                        <?php endif; ?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                            <?php if (!$routines): ?>
                                <tr><td colspan="5" class="text-body-secondary">No hay rutinas registradas.</td></tr>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
