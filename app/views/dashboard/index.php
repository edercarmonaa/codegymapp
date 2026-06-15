<?php
$reportsJson = json_encode($reports, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
$dashboardMetrics = [
    ['Retos cumplidos', $stats['completed_month']],
    ['Cumplimiento general', $stats['general_percent'] . '%'],
    ['Cumplimiento puntual', $stats['on_time_percent'] . '%'],
    ['Tiempo practicado', $stats['time_month'] . ' min'],
    ['Racha actual', $streaks['current'] . ' días'],
    ['Mejor racha', $streaks['best'] . ' días'],
    ['Racha del mes', $streaks['month'] . ' días'],
    ['Retos vencidos', $stats['expired_review']],
];
?>

<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Dashboard</h1>
        <p class="text-body-secondary mb-0">Resumen, gráficas y reportes del seguimiento</p>
    </div>
</div>

<div
    id="dashboardData"
    data-weekly="<?= e(json_encode($weeklyCompliance, JSON_UNESCAPED_UNICODE) ?: '[]') ?>"
    hidden
></div>
<div id="reportsData" data-reports="<?= e($reportsJson) ?>" hidden></div>

<ul class="nav nav-tabs mb-4" id="dashboardTabs" role="tablist">
    <li class="nav-item" role="presentation">
        <button class="nav-link active" id="datos-generales-tab" data-bs-toggle="tab" data-bs-target="#datos-generales" type="button" role="tab" aria-controls="datos-generales" aria-selected="true">
            Datos generales
        </button>
    </li>
    <li class="nav-item" role="presentation">
        <button class="nav-link" id="graficas-tab" data-bs-toggle="tab" data-bs-target="#graficas" type="button" role="tab" aria-controls="graficas" aria-selected="false">
            Gráficas
        </button>
    </li>
    <li class="nav-item" role="presentation">
        <button class="nav-link" id="reportes-tab" data-bs-toggle="tab" data-bs-target="#reportes" type="button" role="tab" aria-controls="reportes" aria-selected="false">
            Reportes
        </button>
    </li>
</ul>

<div class="tab-content" id="dashboardTabsContent">
    <section class="tab-pane fade show active" id="datos-generales" role="tabpanel" aria-labelledby="datos-generales-tab" tabindex="0">
        <h2 class="h5 mb-3">Datos generales</h2>
        <div class="row g-4">
            <div class="col-12 col-xl-5">
                <div class="list-group mb-4">
                    <?php foreach ($dashboardMetrics as $metric): ?>
                        <div class="list-group-item d-flex justify-content-between align-items-center">
                            <span><?= e((string) $metric[0]) ?></span>
                            <strong><?= e((string) $metric[1]) ?></strong>
                        </div>
                    <?php endforeach; ?>
                </div>

                <section>
                    <h3 class="h5">Necesita atención</h3>
                    <div class="list-group">
                        <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" href="/retos?status=expired">
                            <span>Retos vencidos por revisar</span>
                            <span class="badge text-bg-secondary"><?= e((string) $attention['expired']) ?></span>
                        </a>
                        <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" href="/calendario">
                            <span>Pendientes próximos 7 días</span>
                            <span class="badge text-bg-primary"><?= e((string) $attention['pending_week']) ?></span>
                        </a>
                        <div class="list-group-item d-flex justify-content-between align-items-center">
                            <span>Días sin práctica registrada</span>
                            <span class="badge text-bg-<?= (int) $attention['days_without_practice'] > 2 ? 'warning' : 'success' ?>"><?= e((string) $attention['days_without_practice']) ?></span>
                        </div>
                        <?php foreach ($goalAlerts as $goal): ?>
                            <a class="list-group-item list-group-item-action" href="/metas">
                                <strong><?= e($goalTypes[$goal['goal_type']] ?? $goal['goal_type']) ?></strong>
                                <span class="text-body-secondary">
                                    <?= e((string) $goal['current_value']) ?>/<?= e((string) $goal['target_value']) ?>
                                    · <?= e((string) $goal['progress_percent']) ?>%
                                    <?php if ($goal['platform_name'] || $goal['language_name']): ?>
                                        · <?= e(trim(($goal['platform_name'] ?? '') . ' ' . ($goal['language_name'] ?? ''))) ?>
                                    <?php endif; ?>
                                </span>
                            </a>
                        <?php endforeach; ?>
                    </div>
                </section>
            </div>

            <div class="col-12 col-xl-7">
                <div class="row g-4">
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Retos de hoy</h3>
                            <div class="list-group">
                                <?php foreach ($todayChallenges as $challenge): ?>
                                    <a class="list-group-item list-group-item-action" href="/retos">
                                        <strong><?= e($challenge['platform_name']) ?></strong>
                                        <span class="text-body-secondary"><?= e($challenge['title'] ?: 'Pendiente por detallar') ?></span>
                                    </a>
                                <?php endforeach; ?>
                                <?php if (!$todayChallenges): ?>
                                    <div class="list-group-item text-body-secondary">No hay retos pendientes para hoy.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Metas activas</h3>
                            <div class="list-group">
                                <?php foreach ($activeGoals as $goal): ?>
                                    <a class="list-group-item list-group-item-action" href="/metas">
                                        <div class="d-flex justify-content-between gap-3">
                                            <strong><?= e($goalTypes[$goal['goal_type']] ?? $goal['goal_type']) ?></strong>
                                            <span class="text-body-secondary"><?= e((string) $goal['progress_percent']) ?>%</span>
                                        </div>
                                        <div class="progress mt-2" role="progressbar" aria-valuenow="<?= e((string) $goal['progress_percent']) ?>" aria-valuemin="0" aria-valuemax="100">
                                            <div class="progress-bar" style="width: <?= e((string) min(100, (float) $goal['progress_percent'])) ?>%"></div>
                                        </div>
                                        <span class="small text-body-secondary">
                                            <?= e((string) $goal['current_value']) ?>/<?= e((string) $goal['target_value']) ?>
                                            · vence <?= e((string) $goal['period_end']) ?>
                                        </span>
                                    </a>
                                <?php endforeach; ?>
                                <?php if (!$activeGoals): ?>
                                    <div class="list-group-item text-body-secondary">No hay metas activas.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Retos vencidos pendientes de revisar</h3>
                            <div class="list-group">
                                <?php foreach ($expiredChallenges as $challenge): ?>
                                    <a class="list-group-item list-group-item-action" href="/retos">
                                        <strong><?= e($challenge['platform_name']) ?></strong>
                                        <span class="badge text-bg-secondary"><?= e($challenge['scheduled_date']) ?></span>
                                    </a>
                                <?php endforeach; ?>
                                <?php if (!$expiredChallenges): ?>
                                    <div class="list-group-item text-body-secondary">No hay retos vencidos pendientes.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Notificaciones pendientes</h3>
                            <div class="list-group">
                                <?php foreach ($notifications as $notification): ?>
                                    <a class="list-group-item list-group-item-action" href="<?= e(safe_app_url($notification['action_url'] ?? '', '/notificaciones')) ?>">
                                        <strong><?= e($notification['title']) ?></strong>
                                        <span class="d-block text-body-secondary"><?= e($notification['message']) ?></span>
                                    </a>
                                <?php endforeach; ?>
                                <?php if (!$notifications): ?>
                                    <div class="list-group-item text-body-secondary">No hay notificaciones pendientes.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Plataformas del mes</h3>
                            <div class="list-group">
                                <?php foreach ($topPlatforms as $platform): ?>
                                    <div class="list-group-item d-flex justify-content-between align-items-center">
                                        <span><?= e($platform['label']) ?></span>
                                        <span class="text-body-secondary"><?= e((string) $platform['value']) ?> retos · <?= e((string) $platform['minutes']) ?> min</span>
                                    </div>
                                <?php endforeach; ?>
                                <?php if (!$topPlatforms): ?>
                                    <div class="list-group-item text-body-secondary">Sin plataformas completadas este mes.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                    <div class="col-12 col-lg-6">
                        <section>
                            <h3 class="h5">Lenguajes del mes</h3>
                            <div class="list-group">
                                <?php foreach ($topLanguages as $language): ?>
                                    <div class="list-group-item d-flex justify-content-between align-items-center">
                                        <span><?= e($language['label']) ?></span>
                                        <span class="text-body-secondary"><?= e((string) $language['value']) ?> retos · <?= e((string) $language['minutes']) ?> min</span>
                                    </div>
                                <?php endforeach; ?>
                                <?php if (!$topLanguages): ?>
                                    <div class="list-group-item text-body-secondary">Sin lenguajes completados este mes.</div>
                                <?php endif; ?>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="tab-pane fade" id="graficas" role="tabpanel" aria-labelledby="graficas-tab" tabindex="0">
        <h2 class="h5 mb-3">Gráficas</h2>
        <div class="row g-4">
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Cumplimiento semanal</h3>
                    <div class="chart-frame">
                        <canvas id="dashboardWeeklyChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Cumplimiento</h3>
                    <div class="chart-frame">
                        <canvas
                            id="dashboardComplianceChart"
                            data-completed="<?= e((string) $distribution['completed']) ?>"
                            data-missed="<?= e((string) $distribution['missed']) ?>"
                            data-expired="<?= e((string) $distribution['expired']) ?>"
                            data-cancelled="<?= e((string) $distribution['cancelled']) ?>"
                        ></canvas>
                    </div>
                </section>
            </div>
        </div>
    </section>

    <section class="tab-pane fade" id="reportes" role="tabpanel" aria-labelledby="reportes-tab" tabindex="0">
        <h2 class="h5 mb-3">Reportes</h2>
        <form class="row g-2 align-items-end mb-4" method="get" action="/dashboard#reportes">
            <div class="col-12 col-md-2">
                <label class="form-label" for="date_from">Desde</label>
                <input class="form-control" id="date_from" name="date_from" type="date" value="<?= e((string) ($filters['date_from'] ?? '')) ?>">
            </div>
            <div class="col-12 col-md-2">
                <label class="form-label" for="date_to">Hasta</label>
                <input class="form-control" id="date_to" name="date_to" type="date" value="<?= e((string) ($filters['date_to'] ?? '')) ?>">
            </div>
            <div class="col-12 col-md-2">
                <label class="form-label" for="platform_id">Plataforma</label>
                <select class="form-select" id="platform_id" name="platform_id">
                    <option value="0">Todas</option>
                    <?php foreach ($platforms as $platform): ?>
                        <option value="<?= e((string) $platform['id']) ?>" <?= (int) ($filters['platform_id'] ?? 0) === (int) $platform['id'] ? 'selected' : '' ?>><?= e($platform['name']) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-12 col-md-2">
                <label class="form-label" for="language_id">Lenguaje</label>
                <select class="form-select" id="language_id" name="language_id">
                    <option value="0">Todos</option>
                    <?php foreach ($languages as $language): ?>
                        <option value="<?= e((string) $language['id']) ?>" <?= (int) ($filters['language_id'] ?? 0) === (int) $language['id'] ? 'selected' : '' ?>><?= e($language['name']) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-12 col-md-2">
                <label class="form-label" for="status">Estado</label>
                <select class="form-select" id="status" name="status">
                    <option value="">Todos</option>
                    <?php foreach ($statusLabels as $value => $label): ?>
                        <option value="<?= e($value) ?>" <?= ($filters['status'] ?? '') === $value ? 'selected' : '' ?>><?= e($label) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-12 col-md-2">
                <label class="form-label" for="completion_type">Cumplimiento</label>
                <select class="form-select" id="completion_type" name="completion_type">
                    <option value="">Todos</option>
                    <option value="on_time" <?= ($filters['completion_type'] ?? '') === 'on_time' ? 'selected' : '' ?>>A tiempo</option>
                    <option value="late" <?= ($filters['completion_type'] ?? '') === 'late' ? 'selected' : '' ?>>Fuera de fecha</option>
                </select>
            </div>
            <div class="col-12">
                <button class="btn btn-primary">Aplicar filtros</button>
                <a class="btn btn-outline-secondary" href="/dashboard#reportes">Limpiar</a>
            </div>
        </form>

        <div class="list-group mb-4">
            <?php foreach ([
                ['Considerados', $reports['compliance']['scheduled']],
                ['Cumplidos', $reports['compliance']['completed']],
                ['Cumplimiento general', $reports['compliance']['general_percent'] . '%'],
                ['Cumplimiento puntual', $reports['compliance']['on_time_percent'] . '%'],
            ] as $metric): ?>
                <div class="list-group-item d-flex justify-content-between align-items-center">
                    <span><?= e((string) $metric[0]) ?></span>
                    <strong><?= e((string) $metric[1]) ?></strong>
                </div>
            <?php endforeach; ?>
        </div>

        <div class="row g-4">
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Cumplimiento general vs puntual</h3>
                    <div class="chart-frame">
                        <canvas id="reportComplianceChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <div class="d-flex align-items-center justify-content-between gap-3">
                        <h3 class="h5 mb-0">Tiempo practicado por mes</h3>
                        <span class="badge text-bg-primary">
                            <?= e((string) array_sum(array_map(static fn (array $row): int => (int) $row['value'], $reports['timeByMonth']))) ?> min
                        </span>
                    </div>
                    <div class="chart-frame">
                        <canvas id="reportTimeChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Retos cumplidos por plataforma</h3>
                    <div class="chart-frame">
                        <canvas id="reportPlatformsChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Retos cumplidos por lenguaje</h3>
                    <div class="chart-frame">
                        <canvas id="reportLanguagesChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Cumplidos a tiempo vs fuera de fecha</h3>
                    <div class="chart-frame">
                        <canvas id="reportPunctualityChart"></canvas>
                    </div>
                </section>
            </div>
            <div class="col-12 col-xl-6">
                <section class="border rounded-2 p-3 h-100">
                    <h3 class="h5">Historial total por estado</h3>
                    <div class="chart-frame">
                        <canvas id="reportHistoryChart"></canvas>
                    </div>
                </section>
            </div>
        </div>
    </section>
</div>
