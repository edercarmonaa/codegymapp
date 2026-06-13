<section class="login-panel w-100">
    <div class="text-center mb-4">
        <img class="login-logo" src="/public/assets/img/codegym-logo.png" width="260" height="260" alt="CodeGym">
        <p class="text-body-secondary mb-0">Control personal de retos de programación</p>
    </div>

    <?php if (!empty($_SESSION['flash_error'])): ?>
        <div class="alert alert-danger"><?= e((string) $_SESSION['flash_error']) ?></div>
        <?php unset($_SESSION['flash_error']); ?>
    <?php endif; ?>

    <form action="/login" method="post" class="vstack gap-3">
        <?= csrf_field() ?>
        <div>
            <label class="form-label" for="username">Usuario</label>
            <input class="form-control" id="username" name="username" autocomplete="username" required>
        </div>
        <div>
            <label class="form-label" for="password">Contraseña</label>
            <input class="form-control" id="password" name="password" type="password" autocomplete="current-password" required>
        </div>
        <button class="btn btn-primary w-100">Iniciar sesión</button>
    </form>
</section>
