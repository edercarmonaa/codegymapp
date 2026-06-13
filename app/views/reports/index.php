<?php
$reportsJson = json_encode($reports, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
?>

<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Reportes</h1>
        <p class="text-body-secondary mb-0">Gráficas e historial total</p>
    </div>
</div>

<div id="reportsData" data-reports="<?= e($reportsJson) ?>"></div>

<div class="row g-3 mb-4">
    <?php foreach ([
        ['Considerados', $reports['compliance']['scheduled']],
        ['Cumplidos', $reports['compliance']['completed']],
        ['Cumplimiento general', $reports['compliance']['general_percent'] . '%'],
        ['Cumplimiento puntual', $reports['compliance']['on_time_percent'] . '%'],
    ] as $card): ?>
        <div class="col-12 col-sm-6 col-xl-3">
            <div class="metric-card border rounded-2 p-3 h-100">
                <div class="text-body-secondary small"><?= e((string) $card[0]) ?></div>
                <div class="fs-3 fw-semibold"><?= e((string) $card[1]) ?></div>
            </div>
        </div>
    <?php endforeach; ?>
</div>

<div class="row g-4">
    <div class="col-12 col-xl-6">
        <section class="border rounded-2 p-3 h-100">
            <h2 class="h5">Cumplimiento general vs puntual</h2>
            <div class="chart-frame">
                <canvas id="reportComplianceChart"></canvas>
            </div>
        </section>
    </div>
    <div class="col-12 col-xl-6">
        <section class="border rounded-2 p-3 h-100">
            <div class="d-flex align-items-center justify-content-between gap-3">
                <h2 class="h5 mb-0">Tiempo practicado por mes</h2>
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
            <h2 class="h5">Retos cumplidos por plataforma</h2>
            <div class="chart-frame">
                <canvas id="reportPlatformsChart"></canvas>
            </div>
        </section>
    </div>
    <div class="col-12 col-xl-6">
        <section class="border rounded-2 p-3 h-100">
            <h2 class="h5">Retos cumplidos por lenguaje</h2>
            <div class="chart-frame">
                <canvas id="reportLanguagesChart"></canvas>
            </div>
        </section>
    </div>
    <div class="col-12 col-xl-6">
        <section class="border rounded-2 p-3 h-100">
            <h2 class="h5">Cumplidos a tiempo vs fuera de fecha</h2>
            <div class="chart-frame">
                <canvas id="reportPunctualityChart"></canvas>
            </div>
        </section>
    </div>
    <div class="col-12 col-xl-6">
        <section class="border rounded-2 p-3 h-100">
            <h2 class="h5">Historial total por estado</h2>
            <div class="chart-frame">
                <canvas id="reportHistoryChart"></canvas>
            </div>
        </section>
    </div>
</div>
