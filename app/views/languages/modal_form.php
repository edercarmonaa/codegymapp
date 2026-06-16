<form action="/api/languages/save" method="post" class="vstack gap-3" data-api-form>
    <?= csrf_field() ?>
    <input class="form-control" name="name" placeholder="Nombre" required>
    <button class="btn btn-primary">Guardar</button>
</form>
