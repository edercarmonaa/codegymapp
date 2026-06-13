<div class="d-flex flex-wrap gap-3 align-items-end justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Lenguajes</h1>
        <p class="text-body-secondary mb-0">Catálogo de lenguajes de programación</p>
    </div>
    <form class="row g-2 align-items-end" action="/lenguajes/guardar" method="post">
        <?= csrf_field() ?>
        <div class="col-auto"><input class="form-control" name="name" placeholder="Nombre" required></div>
        <div class="col-auto"><button class="btn btn-primary">Guardar</button></div>
    </form>
</div>

<?php require __DIR__ . '/table.php'; ?>

