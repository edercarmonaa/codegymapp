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
    <form class="border rounded-2 p-3" action="/retos/manual" method="post">
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

<form class="row g-2 align-items-end mb-4" method="get" action="/retos">
    <div class="col-12 col-md-3">
        <label class="form-label" for="status">Estado</label>
        <select class="form-select" id="status" name="status">
            <option value="">Todos</option>
            <?php foreach ($statusLabels as $value => $label): ?>
                <option value="<?= e($value) ?>" <?= ($filters['status'] ?? '') === $value ? 'selected' : '' ?>><?= e($label) ?></option>
            <?php endforeach; ?>
        </select>
    </div>
    <div class="col-12 col-md-4">
        <label class="form-label" for="platform_id">Plataforma</label>
        <select class="form-select" id="platform_id" name="platform_id">
            <option value="0">Todas</option>
            <?php foreach ($platforms as $platform): ?>
                <option value="<?= e((string) $platform['id']) ?>" <?= (int) ($filters['platform_id'] ?? 0) === (int) $platform['id'] ? 'selected' : '' ?>>
                    <?= e($platform['name']) ?><?= (int) $platform['is_active'] ? '' : ' (inactiva)' ?>
                </option>
            <?php endforeach; ?>
        </select>
    </div>
    <div class="col-12 col-md-auto">
        <button class="btn btn-outline-primary">Filtrar</button>
        <a class="btn btn-outline-secondary" href="/retos">Limpiar</a>
    </div>
</form>

<div class="table-responsive">
    <table class="table align-middle table-hover">
        <thead>
            <tr>
                <th>Fecha programada</th>
                <th>Plataforma</th>
                <th>Nombre del reto</th>
                <th>Estado</th>
                <th>Dificultad</th>
                <th>Lenguajes</th>
                <th>Tiempo</th>
                <th>Fecha de cumplimiento</th>
                <th>GitHub</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($challenges as $challenge): ?>
                <?php
                $status = (string) $challenge['status'];
                $statusLabel = $statusLabels[$status] ?? $status;
                $badgeClass = $statusBadgeClasses[$status] ?? 'text-bg-secondary';
                $isLate = $status === 'completed'
                    && !empty($challenge['completed_date'])
                    && !empty($challenge['scheduled_date'])
                    && $challenge['completed_date'] > $challenge['scheduled_date'];
                $githubUrls = array_filter(explode("\n", (string) ($challenge['github_urls'] ?? '')));
                ?>
                <tr>
                    <td>
                        <?= e($challenge['scheduled_date']) ?>
                        <?php if ((int) $challenge['is_rescheduled'] === 1): ?>
                            <span class="badge text-bg-info">Reprogramado</span>
                        <?php endif; ?>
                        <?php if ($challenge['origin'] === 'manual'): ?>
                            <span class="badge text-bg-success">Manual</span>
                        <?php endif; ?>
                    </td>
                    <td><?= e($challenge['platform_name']) ?></td>
                    <td>
                        <div class="fw-semibold"><?= e($challenge['title'] ?: 'Pendiente por detallar') ?></div>
                        <?php if (!empty($challenge['challenge_url'])): ?>
                            <a class="small" href="<?= e($challenge['challenge_url']) ?>" target="_blank" rel="noopener">Ver reto</a>
                        <?php endif; ?>
                    </td>
                    <td>
                        <span class="badge <?= e($badgeClass) ?>"><?= e($statusLabel) ?></span>
                        <?php if ($isLate): ?>
                            <span class="badge text-bg-warning">Fuera de fecha</span>
                        <?php endif; ?>
                    </td>
                    <td><?= e($challenge['difficulty'] ?: '-') ?></td>
                    <td><?= e($challenge['language_names'] ?: '-') ?></td>
                    <td><?= $challenge['time_spent_minutes'] ? e((string) $challenge['time_spent_minutes']) . ' min' : '-' ?></td>
                    <td><?= e($challenge['completed_date'] ?: '-') ?></td>
                    <td>
                        <?php if ($githubUrls): ?>
                            <div class="d-flex flex-column gap-1">
                                <?php foreach ($githubUrls as $index => $url): ?>
                                    <a href="<?= e($url) ?>" target="_blank" rel="noopener">Solución <?= e((string) ($index + 1)) ?></a>
                                <?php endforeach; ?>
                            </div>
                        <?php else: ?>
                            <span class="text-body-secondary">-</span>
                        <?php endif; ?>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$challenges): ?>
                <tr><td colspan="9" class="text-body-secondary">No hay retos con esos filtros.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>
