<form action="/usuario/cambiar-password" method="post" class="vstack gap-3">
    <?= csrf_field() ?>
    <input class="form-control" type="password" name="password" placeholder="Nueva contraseña" required>
    <button class="btn btn-warning">Cambiar contraseña</button>
</form>

