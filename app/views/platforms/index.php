<div class="d-flex flex-wrap gap-3 align-items-end justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Plataformas</h1>
        <p class="text-body-secondary mb-0">Catálogo de sitios de retos</p>
    </div>
    <form class="row g-2 align-items-end" action="/api/platforms/save" method="post" data-api-form>
        <?= csrf_field() ?>
        <div class="col-auto"><input class="form-control" name="name" placeholder="Nombre" required></div>
        <div class="col-auto"><input class="form-control" name="url" placeholder="URL"></div>
        <div class="col-auto"><input class="form-control" name="description" placeholder="Descripción"></div>
        <div class="col-auto"><button class="btn btn-primary">Guardar</button></div>
    </form>
</div>

<div id="tablePanel">
    <?php require __DIR__ . '/table.php'; ?>
</div>
