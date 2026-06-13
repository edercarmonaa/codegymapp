<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Metas</h1>
        <p class="text-body-secondary mb-0">Metas semanales, mensuales y anuales</p>
    </div>
</div>

<form class="border rounded-2 p-3 mb-4" action="/metas/guardar" method="post">
    <?= csrf_field() ?>
    <div class="row g-3 align-items-end">
        <div class="col-12 col-md-3">
            <label class="form-label" for="goal_type">Tipo de meta</label>
            <select class="form-select" id="goal_type" name="goal_type" required>
                <?php foreach ($goalTypes as $value => $label): ?>
                    <option value="<?= e($value) ?>"><?= e($label) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-12 col-md-2">
            <label class="form-label" for="period_type">Periodo</label>
            <select class="form-select" id="period_type" name="period_type" required>
                <?php foreach ($periodTypes as $value => $label): ?>
                    <option value="<?= e($value) ?>"><?= e($label) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-12 col-md-2">
            <label class="form-label" for="target_value">Objetivo</label>
            <input class="form-control" id="target_value" name="target_value" type="number" min="1" step="1" required>
        </div>
        <div class="col-12 col-md-2">
            <label class="form-label" for="platform_id">Plataforma</label>
            <select class="form-select" id="platform_id" name="platform_id">
                <option value="0">General</option>
                <?php foreach ($platforms as $platform): ?>
                    <option value="<?= e((string) $platform['id']) ?>"><?= e($platform['name']) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-12 col-md-2">
            <label class="form-label" for="language_id">Lenguaje</label>
            <select class="form-select" id="language_id" name="language_id">
                <option value="0">General</option>
                <?php foreach ($languages as $language): ?>
                    <option value="<?= e((string) $language['id']) ?>"><?= e($language['name']) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-12 col-md-1">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" id="auto_renew" name="auto_renew" value="1">
                <label class="form-check-label" for="auto_renew">Renovar</label>
            </div>
        </div>
        <div class="col-12">
            <button class="btn btn-primary">Crear meta</button>
        </div>
    </div>
</form>

<?php require __DIR__ . '/table.php'; ?>
