<?php
$bulkTable = 'platforms';
$bulkActions = [
    ['label' => 'Activar', 'url' => '/api/platforms/activate', 'refresh' => 'platforms', 'class' => 'btn-outline-success'],
    ['label' => 'Desactivar', 'url' => '/api/platforms/deactivate', 'refresh' => 'platforms', 'class' => 'btn-outline-secondary', 'confirm' => '¿Desactivar las plataformas seleccionadas?'],
];
require __DIR__ . '/../partials/table_pagination.php';
?>

<div class="table-responsive">
    <table class="table align-middle">
        <thead>
            <tr><th class="text-center"><input class="form-check-input" type="checkbox" data-bulk-select-all="platforms" aria-label="Seleccionar todas las plataformas visibles"></th><th><a href="?sort=name&dir=asc">Nombre</a></th><th>URL</th><th>Descripción</th><th><a href="?sort=is_active&dir=desc">Estado</a></th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
            <?php foreach ($platforms as $platform): ?>
                <tr>
                    <td class="text-center"><input class="form-check-input" type="checkbox" data-bulk-item="platforms" value="<?= e((string) $platform['id']) ?>" aria-label="Seleccionar plataforma"></td>
                    <td><?= e($platform['name']) ?></td>
                    <td>
                        <?php $platformUrl = safe_url($platform['url'] ?? null); ?>
                        <?php if ($platformUrl): ?>
                            <a href="<?= e($platformUrl) ?>" target="_blank" rel="noopener">Abrir</a>
                        <?php endif; ?>
                    </td>
                    <td><?= e($platform['description']) ?></td>
                    <td><span class="badge <?= $platform['is_active'] ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= $platform['is_active'] ? 'Activa' : 'Inactiva' ?></span></td>
                    <td class="text-end">
                        <form class="d-inline" action="<?= $platform['is_active'] ? '/api/platforms/deactivate' : '/api/platforms/activate' ?>" method="post" data-api-form data-api-refresh-catalog="platforms" <?= $platform['is_active'] ? 'data-confirm="¿Desactivar esta plataforma?"' : '' ?>>
                            <?= csrf_field() ?>
                            <input type="hidden" name="id" value="<?= e((string) $platform['id']) ?>">
                            <button class="btn btn-sm btn-outline-secondary"><?= $platform['is_active'] ? 'Desactivar' : 'Activar' ?></button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
