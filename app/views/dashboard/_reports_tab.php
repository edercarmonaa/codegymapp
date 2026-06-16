<?php
$reportsJson = json_encode($reports, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
?>

<section class="tab-pane fade<?= !empty($reportsTabActive) ? ' show active' : '' ?>" id="reportes" role="tabpanel" aria-labelledby="reportes-tab" tabindex="0">
    <div id="reportsData" data-reports="<?= e($reportsJson) ?>" hidden></div>
    <h2 class="h5 mb-3">Reportes</h2>
    <form
        class="row g-2 align-items-end mb-4"
        method="get"
        action="/dashboard#reportes"
        hx-get="/dashboard/reportes"
        hx-target="#reportes"
        hx-swap="outerHTML"
    >
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
            <a class="btn btn-outline-secondary" href="/dashboard#reportes" hx-get="/dashboard/reportes" hx-target="#reportes" hx-swap="outerHTML">Limpiar</a>
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
