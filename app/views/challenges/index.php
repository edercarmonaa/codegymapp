<div class="d-flex flex-wrap gap-3 align-items-end justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Retos</h1>
        <p class="text-body-secondary mb-0">Historial y seguimiento de retos registrados</p>
    </div>
    <a class="btn btn-primary" href="/calendario">Abrir calendario</a>
</div>

<form class="row g-2 align-items-end mb-4" method="get" action="/retos">
    <div class="col-12 col-md-3">
        <label class="form-label" for="status">Estado</label>
        <select class="form-select" id="status" name="status">
            <option value="">Todos</option>
            <?php foreach ($statusLabels as $value => $label): ?>
                <option value="<?= e($value) ?>" <?= ($filters['status'] ?? '') === $value ? 'selected' : '' ?>><?= e($label) ?></option>
            <?php endforeach; ?>
        </select>
    </div>
    <div class="col-12 col-md-4">
        <label class="form-label" for="platform_id">Plataforma</label>
        <select class="form-select" id="platform_id" name="platform_id">
            <option value="0">Todas</option>
            <?php foreach ($platforms as $platform): ?>
                <option value="<?= e((string) $platform['id']) ?>" <?= (int) ($filters['platform_id'] ?? 0) === (int) $platform['id'] ? 'selected' : '' ?>>
                    <?= e($platform['name']) ?><?= (int) $platform['is_active'] ? '' : ' (inactiva)' ?>
                </option>
            <?php endforeach; ?>
        </select>
    </div>
    <div class="col-12 col-md-auto">
        <button class="btn btn-outline-primary">Filtrar</button>
        <a class="btn btn-outline-secondary" href="/retos">Limpiar</a>
    </div>
</form>

<div class="table-responsive">
    <table class="table align-middle table-hover">
        <thead>
            <tr>
                <th>Fecha programada</th>
                <th>Plataforma</th>
                <th>Nombre del reto</th>
                <th>Estado</th>
                <th>Dificultad</th>
                <th>Lenguajes</th>
                <th>Tiempo</th>
                <th>Fecha de cumplimiento</th>
                <th>GitHub</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($challenges as $challenge): ?>
                <?php
                $status = (string) $challenge['status'];
                $statusLabel = $statusLabels[$status] ?? $status;
                $badgeClass = $statusBadgeClasses[$status] ?? 'text-bg-secondary';
                $isLate = $status === 'completed'
                    && !empty($challenge['completed_date'])
                    && !empty($challenge['scheduled_date'])
                    && $challenge['completed_date'] > $challenge['scheduled_date'];
                $githubUrls = array_filter(explode("\n", (string) ($challenge['github_urls'] ?? '')));
                ?>
                <tr>
                    <td>
                        <?= e($challenge['scheduled_date']) ?>
                        <?php if ((int) $challenge['is_rescheduled'] === 1): ?>
                            <span class="badge text-bg-info">Reprogramado</span>
                        <?php endif; ?>
                    </td>
                    <td><?= e($challenge['platform_name']) ?></td>
                    <td>
                        <div class="fw-semibold"><?= e($challenge['title'] ?: 'Pendiente por detallar') ?></div>
                        <?php if (!empty($challenge['challenge_url'])): ?>
                            <a class="small" href="<?= e($challenge['challenge_url']) ?>" target="_blank" rel="noopener">Ver reto</a>
                        <?php endif; ?>
                    </td>
                    <td>
                        <span class="badge <?= e($badgeClass) ?>"><?= e($statusLabel) ?></span>
                        <?php if ($isLate): ?>
                            <span class="badge text-bg-warning">Fuera de fecha</span>
                        <?php endif; ?>
                    </td>
                    <td><?= e($challenge['difficulty'] ?: '-') ?></td>
                    <td><?= e($challenge['language_names'] ?: '-') ?></td>
                    <td><?= $challenge['time_spent_minutes'] ? e((string) $challenge['time_spent_minutes']) . ' min' : '-' ?></td>
                    <td><?= e($challenge['completed_date'] ?: '-') ?></td>
                    <td>
                        <?php if ($githubUrls): ?>
                            <div class="d-flex flex-column gap-1">
                                <?php foreach ($githubUrls as $index => $url): ?>
                                    <a href="<?= e($url) ?>" target="_blank" rel="noopener">Solución <?= e((string) ($index + 1)) ?></a>
                                <?php endforeach; ?>
                            </div>
                        <?php else: ?>
                            <span class="text-body-secondary">-</span>
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$challenges): ?>
                <tr><td colspan="9" class="text-body-secondary">No hay retos con esos filtros.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>
