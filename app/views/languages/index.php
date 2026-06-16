<div class="d-flex flex-wrap gap-3 align-items-end justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Lenguajes</h1>
        <p class="text-body-secondary mb-0">Catálogo de lenguajes de programación</p>
    </div>
    <form class="row g-2 align-items-end" action="/api/languages/save" method="post" data-api-form data-api-refresh-catalog="languages">
        <?= csrf_field() ?>
        <div class="col-auto"><input class="form-control" name="name" placeholder="Nombre" required></div>
        <div class="col-auto"><button class="btn btn-primary">Guardar</button></div>
    </form>
</div>

<div id="tablePanel" data-catalog-panel="languages" data-catalog-url="/api/catalogs/languages/list">
    <?php require __DIR__ . '/table.php'; ?>
</div>
