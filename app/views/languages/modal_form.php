<form action="/lenguajes/guardar" method="post" class="vstack gap-3">
    <?= csrf_field() ?>
    <input class="form-control" name="name" placeholder="Nombre" required>
    <button class="btn btn-primary">Guardar</button>
</form>

