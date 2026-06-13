<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Dashboard</h1>
        <p class="text-body-secondary mb-0">Resumen del mes actual</p>
    </div>
</div>

<div class="row g-3 mb-4">
    <?php foreach ([
        ['Retos cumplidos', $stats['completed_month']],
        ['Cumplimiento general', $stats['general_percent'] . '%'],
        ['Cumplimiento puntual', $stats['on_time_percent'] . '%'],
        ['Tiempo practicado', $stats['time_month'] . ' min'],
        ['Racha actual', $streaks['current'] . ' días'],
        ['Mejor racha', $streaks['best'] . ' días'],
        ['Racha del mes', $streaks['month'] . ' días'],
        ['Retos vencidos', $stats['expired_review']],
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
    <div class="col-12 col-xl-5">
        <section class="mb-4">
            <h2 class="h5">Retos de hoy</h2>
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

        <section>
            <h2 class="h5">Retos vencidos pendientes de revisar</h2>
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
    <div class="col-12 col-xl-7">
        <section>
            <h2 class="h5">Cumplimiento</h2>
            <div class="chart-frame border rounded-2 p-3">
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
