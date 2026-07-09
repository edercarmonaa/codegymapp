<?php
$bulkTable = 'languages';
$bulkActions = [
    ['label' => 'Activar', 'url' => '/api/languages/activate', 'refresh' => 'languages', 'class' => 'btn-outline-success'],
    ['label' => 'Desactivar', 'url' => '/api/languages/deactivate', 'refresh' => 'languages', 'class' => 'btn-outline-secondary', 'confirm' => '¿Desactivar los lenguajes seleccionados?'],
];
require __DIR__ . '/../partials/table_pagination.php';
?>

<div class="table-responsive">
    <table class="table align-middle">
        <thead>
            <tr><th class="text-center"><input class="form-check-input" type="checkbox" data-bulk-select-all="languages" aria-label="Seleccionar todos los lenguajes visibles"></th><th><a href="?sort=name&dir=asc">Nombre</a></th><th><a href="?sort=is_active&dir=desc">Estado</a></th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
            <?php foreach ($languages as $language): ?>
                <tr>
                    <td class="text-center"><input class="form-check-input" type="checkbox" data-bulk-item="languages" value="<?= e((string) $language['id']) ?>" aria-label="Seleccionar lenguaje"></td>
                    <td><?= e($language['name']) ?></td>
                    <td><span class="badge <?= $language['is_active'] ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= $language['is_active'] ? 'Activo' : 'Inactivo' ?></span></td>
                    <td class="text-end">
                        <form class="d-inline" action="<?= $language['is_active'] ? '/api/languages/deactivate' : '/api/languages/activate' ?>" method="post" data-api-form data-api-refresh-catalog="languages" <?= $language['is_active'] ? 'data-confirm="¿Desactivar este lenguaje?"' : '' ?>>
                            <?= csrf_field() ?>
                            <input type="hidden" name="id" value="<?= e((string) $language['id']) ?>">
                            <button class="btn btn-sm btn-outline-secondary"><?= $language['is_active'] ? 'Desactivar' : 'Activar' ?></button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</div>

<?php require __DIR__ . '/../partials/table_pagination.php'; ?>
