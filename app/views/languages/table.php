<div class="table-responsive">
    <table class="table align-middle">
        <thead>
            <tr><th>Nombre</th><th>Estado</th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
            <?php foreach ($languages as $language): ?>
                <tr>
                    <td><?= e($language['name']) ?></td>
                    <td><span class="badge <?= $language['is_active'] ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= $language['is_active'] ? 'Activo' : 'Inactivo' ?></span></td>
                    <td class="text-end">
                        <form class="d-inline" action="<?= $language['is_active'] ? '/lenguajes/desactivar' : '/lenguajes/activar' ?>" method="post">
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

