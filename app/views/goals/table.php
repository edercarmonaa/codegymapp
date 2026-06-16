<?php require __DIR__ . '/../partials/table_pagination.php'; ?>

<div class="table-responsive">
    <table class="table align-middle table-hover">
        <thead>
            <tr>
                <th><a href="?sort=goal_type&dir=asc">Meta</a></th>
                <th><a href="?sort=period_end&dir=asc">Periodo</a></th>
                <th>Alcance</th>
                <th><a href="?sort=progress_percent&dir=desc">Avance</a></th>
                <th><a href="?sort=status&dir=asc">Estado</a></th>
                <th class="text-end">Acciones</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($goals as $goal): ?>
                <?php
                $statusClass = $goal['status'] === 'active' ? 'text-bg-success' : 'text-bg-secondary';
                $statusLabel = $goal['status'] === 'active' ? 'Activa' : 'Cerrada';
                $unit = $goal['goal_type'] === 'practice_time' ? 'min' : ($goal['goal_type'] === 'streak' ? 'días' : 'retos');
                ?>
                <tr>
                    <td>
                        <div class="fw-semibold"><?= e($goalTypes[$goal['goal_type']] ?? $goal['goal_type']) ?></div>
                        <div class="text-body-secondary small">Objetivo: <?= e((string) $goal['target_value']) ?> <?= e($unit) ?></div>
                    </td>
                    <td>
                        <div><?= e($periodTypes[$goal['period_type']] ?? $goal['period_type']) ?></div>
                        <div class="text-body-secondary small"><?= e($goal['period_start']) ?> a <?= e($goal['period_end']) ?></div>
                    </td>
                    <td>
                        <div><?= e($goal['platform_name'] ?: 'Todas las plataformas') ?></div>
                        <div class="text-body-secondary small"><?= e($goal['language_name'] ?: 'Todos los lenguajes') ?></div>
                    </td>
                    <td style="min-width: 220px;">
                        <div class="d-flex justify-content-between small mb-1">
                            <span><?= e((string) $goal['current_value']) ?> / <?= e((string) $goal['target_value']) ?> <?= e($unit) ?></span>
                            <span><?= e((string) $goal['progress_percent']) ?>%</span>
                        </div>
                        <div class="progress" role="progressbar" aria-valuenow="<?= e((string) $goal['progress_percent']) ?>" aria-valuemin="0" aria-valuemax="100">
                            <div class="progress-bar" style="width: <?= e((string) min(100, (float) $goal['progress_percent'])) ?>%"></div>
                        </div>
                    </td>
                    <td>
                        <span class="badge <?= e($statusClass) ?>"><?= e($statusLabel) ?></span>
                        <?php if ((int) $goal['auto_renew'] === 1): ?>
                            <span class="badge text-bg-info">Renovable</span>
                        <?php endif; ?>
                    </td>
                    <td class="text-end">
                        <?php if ($goal['status'] === 'active'): ?>
                            <form class="d-inline" action="/api/goals/deactivate" method="post" data-api-form data-api-refresh-catalog="goals" data-confirm="¿Desactivar esta meta?">
                                <?= csrf_field() ?>
                                <input type="hidden" name="id" value="<?= e((string) $goal['id']) ?>">
                                <button class="btn btn-sm btn-outline-secondary">Desactivar</button>
                            </form>
                        <?php else: ?>
                            <span class="text-body-secondary">-</span>
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$goals): ?>
                <tr><td colspan="6" class="text-body-secondary">No hay metas registradas.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
