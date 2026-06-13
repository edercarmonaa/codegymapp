<div class="table-responsive">
    <table class="table align-middle table-hover">
        <thead>
            <tr>
                <th><a href="?sort=title&dir=asc">Notificación</a></th>
                <th><a href="?sort=is_read&dir=asc">Estado</a></th>
                <th><a href="?sort=created_at&dir=desc">Fecha</a></th>
                <th class="text-end">Acciones</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($notifications as $notification): ?>
                <tr>
                    <td>
                        <div class="fw-semibold"><?= e($notification['title']) ?></div>
                        <div class="text-body-secondary"><?= e($notification['message']) ?></div>
                        <?php if (!empty($notification['action_url'])): ?>
                            <a class="small" href="<?= e($notification['action_url']) ?>">Abrir</a>
                        <?php endif; ?>
                    </td>
                    <td>
                        <span class="badge <?= (int) $notification['is_read'] === 1 ? 'text-bg-secondary' : 'text-bg-primary' ?>">
                            <?= (int) $notification['is_read'] === 1 ? 'Leída' : 'Pendiente' ?>
                        </span>
                    </td>
                    <td>
                        <div><?= e($notification['created_at']) ?></div>
                        <?php if (!empty($notification['read_at'])): ?>
                            <div class="small text-body-secondary">Leída: <?= e($notification['read_at']) ?></div>
                        <?php endif; ?>
                    </td>
                    <td class="text-end">
                        <?php if ((int) $notification['is_read'] === 0): ?>
                            <form class="d-inline" action="/notificaciones/marcar-leida" method="post">
                                <?= csrf_field() ?>
                                <input type="hidden" name="id" value="<?= e((string) $notification['id']) ?>">
                                <button class="btn btn-sm btn-outline-primary">Marcar leída</button>
                            </form>
                        <?php else: ?>
                            <form class="d-inline" action="/notificaciones/eliminar" method="post" data-confirm="¿Eliminar esta notificación del historial?">
                                <?= csrf_field() ?>
                                <input type="hidden" name="id" value="<?= e((string) $notification['id']) ?>">
                                <button class="btn btn-sm btn-outline-danger">Eliminar</button>
                            </form>
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$notifications): ?>
                <tr><td colspan="4" class="text-body-secondary">No hay notificaciones registradas.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
