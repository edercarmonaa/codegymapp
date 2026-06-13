<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Mi usuario</h1>
        <p class="text-body-secondary mb-0">Datos de acceso y preferencia de tema</p>
    </div>
</div>

<div class="row g-4">
    <div class="col-12 col-xl-6">
        <form class="border rounded-2 p-3" action="/usuario/actualizar" method="post">
            <?= csrf_field() ?>
            <h2 class="h5">Perfil</h2>
            <div class="mb-3">
                <label class="form-label">Nombre</label>
                <input class="form-control" name="name" value="<?= e($user['name'] ?? '') ?>" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Usuario</label>
                <input class="form-control" name="username" value="<?= e($user['username'] ?? '') ?>" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Correo</label>
                <input class="form-control" type="email" name="email" value="<?= e($user['email'] ?? '') ?>" required>
            </div>
            <button class="btn btn-primary">Actualizar</button>
        </form>
    </div>
    <div class="col-12 col-xl-6">
        <form class="border rounded-2 p-3" action="/usuario/cambiar-password" method="post">
            <?= csrf_field() ?>
            <h2 class="h5">Cambiar contraseña</h2>
            <div class="mb-3">
                <label class="form-label">Nueva contraseña</label>
                <input class="form-control" type="password" name="password" required>
            </div>
            <button class="btn btn-warning">Cambiar contraseña</button>
        </form>
        <div class="border rounded-2 p-3 mt-4">
            <h2 class="h5">Estado</h2>
            <dl class="row mb-0">
                <dt class="col-sm-5">Último inicio</dt><dd class="col-sm-7"><?= e($user['last_login_at'] ?? 'Sin registro') ?></dd>
                <dt class="col-sm-5">Intentos fallidos</dt><dd class="col-sm-7"><?= e((string) ($user['failed_login_attempts'] ?? 0)) ?></dd>
                <dt class="col-sm-5">Bloqueado hasta</dt><dd class="col-sm-7"><?= e($user['locked_until'] ?? 'No bloqueado') ?></dd>
            </dl>
        </div>
    </div>
</div>

