<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Seguridad</h1>
        <p class="text-body-secondary mb-0">Bitácora de accesos y eventos sensibles</p>
    </div>
</div>

<div class="table-responsive">
    <table class="table align-middle">
        <thead>
            <tr><th><a href="?sort=created_at&dir=desc">Fecha</a></th><th>Usuario</th><th><a href="?sort=event_type&dir=asc">Evento</a></th><th><a href="?sort=result&dir=asc">Resultado</a></th><th><a href="?sort=ip_address&dir=asc">IP</a></th><th>Descripción</th></tr>
        </thead>
        <tbody>
            <?php foreach ($logs as $log): ?>
                <tr>
                    <td><?= e($log['created_at']) ?></td>
                    <td><?= e($log['username']) ?></td>
                    <td><?= e($log['event_type']) ?></td>
                    <td><?= e($log['result']) ?></td>
                    <td><?= e($log['ip_address']) ?></td>
                    <td><?= e($log['description']) ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$logs): ?>
                <tr><td colspan="6" class="text-body-secondary">Todavía no hay eventos registrados.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
