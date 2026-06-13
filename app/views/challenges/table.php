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
        <a class="btn btn-outline-secondary" data-table-link="1" href="/retos">Limpiar</a>
    </div>
</form>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>

<div class="table-responsive">
    <table class="table align-middle table-hover">
        <thead>
            <tr>
                <th><a href="?<?= e(http_build_query(array_merge($_GET, ['sort' => 'scheduled_date', 'dir' => 'desc', 'page' => 1]))) ?>">Fecha programada</a></th>
                <th><a href="?<?= e(http_build_query(array_merge($_GET, ['sort' => 'platform', 'dir' => 'asc', 'page' => 1]))) ?>">Plataforma</a></th>
                <th>Nombre del reto</th>
                <th><a href="?<?= e(http_build_query(array_merge($_GET, ['sort' => 'status', 'dir' => 'asc', 'page' => 1]))) ?>">Estado</a></th>
                <th>Dificultad</th>
                <th>Lenguajes</th>
                <th><a href="?<?= e(http_build_query(array_merge($_GET, ['sort' => 'time_spent_minutes', 'dir' => 'desc', 'page' => 1]))) ?>">Tiempo</a></th>
                <th><a href="?<?= e(http_build_query(array_merge($_GET, ['sort' => 'completed_date', 'dir' => 'desc', 'page' => 1]))) ?>">Fecha de cumplimiento</a></th>
                <th>GitHub</th>
                <th class="text-end">Acciones</th>
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
                $githubUrls = array_values(array_filter(array_map('safe_url', explode("\n", (string) ($challenge['github_urls'] ?? '')))));
                ?>
                <tr>
                    <td>
                        <?= e($challenge['scheduled_date']) ?>
                        <?php if ((int) $challenge['is_rescheduled'] === 1): ?>
                            <span class="badge text-bg-info">Reprogramado</span>
                        <?php endif; ?>
                        <?php if ($challenge['origin'] === 'manual'): ?>
                            <span class="badge text-bg-success">Manual</span>
                        <?php endif; ?>
                    </td>
                    <td><?= e($challenge['platform_name']) ?></td>
                    <td>
                        <div class="fw-semibold"><?= e($challenge['title'] ?: 'Pendiente por detallar') ?></div>
                        <?php $challengeUrl = safe_url($challenge['challenge_url'] ?? null); ?>
                        <?php if ($challengeUrl): ?>
                            <a class="small" href="<?= e($challengeUrl) ?>" target="_blank" rel="noopener">Ver reto</a>
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
                    <td class="text-end">
                        <a class="btn btn-sm btn-outline-primary" href="/calendario">Editar</a>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$challenges): ?>
                <tr><td colspan="10" class="text-body-secondary">No hay retos con esos filtros.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
