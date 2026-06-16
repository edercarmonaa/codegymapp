<div class="d-flex flex-wrap gap-3 align-items-end justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Retos</h1>
        <p class="text-body-secondary mb-0">Historial y seguimiento de retos registrados</p>
    </div>
    <div class="d-flex gap-2">
        <button class="btn btn-success" type="button" data-bs-toggle="collapse" data-bs-target="#manualChallengeForm" aria-expanded="false" aria-controls="manualChallengeForm">Registrar realizado</button>
        <a class="btn btn-primary" href="/calendario">Abrir calendario</a>
    </div>
</div>

<div class="collapse mb-4" id="manualChallengeForm">
    <form class="border rounded-2 p-3" action="/api/challenges/manual" method="post" data-api-form data-api-success-url="/retos?status=completed">
        <?= csrf_field() ?>
        <div class="row g-3">
            <div class="col-12 col-md-4">
                <label class="form-label" for="manual_platform_id">Plataforma</label>
                <select class="form-select" id="manual_platform_id" name="platform_id" required>
                    <option value="">Selecciona una plataforma</option>
                    <?php foreach ($activePlatforms as $platform): ?>
                        <option value="<?= e((string) $platform['id']) ?>"><?= e($platform['name']) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-12 col-md-8">
                <label class="form-label" for="manual_title">Nombre del reto</label>
                <input class="form-control" id="manual_title" name="title" maxlength="180" required>
            </div>
            <div class="col-12 col-md-4">
                <label class="form-label" for="manual_difficulty">Dificultad</label>
                <input class="form-control" id="manual_difficulty" name="difficulty" maxlength="120" required>
            </div>
            <div class="col-12 col-md-4">
                <label class="form-label" for="manual_time">Tiempo invertido</label>
                <div class="input-group">
                    <input class="form-control" id="manual_time" name="time_spent_minutes" type="number" min="1" step="1" required>
                    <span class="input-group-text">min</span>
                </div>
            </div>
            <div class="col-12 col-md-4">
                <label class="form-label" for="manual_challenge_url">Enlace del reto</label>
                <input class="form-control" id="manual_challenge_url" name="challenge_url" type="url" maxlength="255">
            </div>
            <div class="col-12">
                <label class="form-label">Lenguajes</label>
                <div class="row g-2">
                    <?php foreach ($activeLanguages as $language): ?>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="language_ids[]" value="<?= e((string) $language['id']) ?>" id="manual_language_<?= e((string) $language['id']) ?>">
                                <label class="form-check-label" for="manual_language_<?= e((string) $language['id']) ?>"><?= e($language['name']) ?></label>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            </div>
            <div class="col-12 col-lg-6">
                <label class="form-label" for="manual_github_links">GitHub</label>
                <textarea class="form-control" id="manual_github_links" name="github_links" rows="4" placeholder="Un enlace por línea"></textarea>
            </div>
            <div class="col-12 col-lg-6">
                <label class="form-label" for="manual_notes">Notas</label>
                <textarea class="form-control" id="manual_notes" name="notes" rows="4"></textarea>
            </div>
            <div class="col-12">
                <button class="btn btn-success">Guardar reto realizado</button>
            </div>
        </div>
    </form>
</div>

<div id="tablePanel">
    <?php require __DIR__ . '/table.php'; ?>
</div>
