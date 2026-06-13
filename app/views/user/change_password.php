<form action="/usuario/cambiar-password" method="post" class="vstack gap-3">
    <?= csrf_field() ?>
    <input class="form-control" type="password" name="current_password" placeholder="Contraseña actual" autocomplete="current-password" required>
    <input class="form-control" type="password" name="password" placeholder="Nueva contraseña" autocomplete="new-password" required>
    <button class="btn btn-warning">Cambiar contraseña</button>
</form>
