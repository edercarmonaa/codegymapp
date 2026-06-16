<form action="/api/platforms/save" method="post" class="vstack gap-3" data-api-form>
    <?= csrf_field() ?>
    <input class="form-control" name="name" placeholder="Nombre" required>
    <input class="form-control" name="url" placeholder="URL">
    <input class="form-control" name="description" placeholder="Descripción">
    <button class="btn btn-primary">Guardar</button>
</form>
