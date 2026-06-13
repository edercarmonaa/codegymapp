<div class="modal fade" id="challengeModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="challengeModalTitle">Reto</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form id="challengeForm">
                <div class="modal-body">
                    <?= csrf_field() ?>
                    <input type="hidden" name="id" id="challengeId">
                    <input type="hidden" name="scheduled_date" id="challengeScheduledDate">
                    <div id="challengeAlert" class="alert alert-danger d-none"></div>

                    <ul class="nav nav-tabs" role="tablist">
                        <li class="nav-item"><button class="nav-link active" id="challengeDataTab" data-bs-toggle="tab" data-bs-target="#challenge-data" type="button">Datos del reto</button></li>
                        <li class="nav-item edit-only"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-languages" type="button">Lenguajes</button></li>
                        <li class="nav-item edit-only"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-github" type="button">GitHub</button></li>
                        <li class="nav-item edit-only"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-notes" type="button">Notas</button></li>
                    </ul>
                    <div class="tab-content border border-top-0 p-3">
                        <div class="tab-pane fade show active" id="challenge-data">
                            <div class="row g-3">
                                <div class="col-12 col-md-6">
                                    <label class="form-label" for="challengePlatform">Plataforma</label>
                                    <select class="form-select" name="platform_id" id="challengePlatform" required>
                                        <option value="">Selecciona una plataforma</option>
                                        <?php foreach ($platforms as $platform): ?>
                                            <option value="<?= e((string) $platform['id']) ?>" data-active="<?= e((string) $platform['is_active']) ?>" <?= (int) $platform['is_active'] ? '' : 'disabled' ?>>
                                                <?= e($platform['name']) ?><?= (int) $platform['is_active'] ? '' : ' (inactiva)' ?>
                                            </option>
                                        <?php endforeach; ?>
                                    </select>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label">Fecha programada</label>
                                    <input class="form-control" id="challengeScheduledDateLabel" disabled>
                                </div>
                                <div class="col-12 edit-only">
                                    <label class="form-label" for="challengeTitle">Nombre del reto</label>
                                    <input class="form-control" name="title" id="challengeTitle" maxlength="180">
                                </div>
                                <div class="col-12 col-md-6 edit-only">
                                    <label class="form-label" for="challengeDifficulty">Dificultad</label>
                                    <input class="form-control" name="difficulty" id="challengeDifficulty" maxlength="120">
                                </div>
                                <div class="col-12 col-md-6 edit-only">
                                    <label class="form-label" for="challengeTime">Tiempo invertido</label>
                                    <div class="input-group">
                                        <input class="form-control" name="time_spent_minutes" id="challengeTime" type="number" min="1" step="1">
                                        <span class="input-group-text">min</span>
                                    </div>
                                </div>
                                <div class="col-12 edit-only">
                                    <label class="form-label" for="challengeUrl">Enlace del reto</label>
                                    <input class="form-control" name="challenge_url" id="challengeUrl" type="url" maxlength="255">
                                </div>
                                <div class="col-12 edit-only">
                                    <span class="badge text-bg-secondary" id="challengeStatusBadge">Pendiente</span>
                                </div>
                            </div>
                        </div>
                        <div class="tab-pane fade edit-only" id="challenge-languages">
                            <div class="row g-2">
                                <?php foreach ($languages as $language): ?>
                                    <div class="col-12 col-sm-6 col-md-4">
                                        <div class="form-check">
                                            <input class="form-check-input challenge-language" type="checkbox" name="language_ids[]" value="<?= e((string) $language['id']) ?>" id="challengeLanguage<?= e((string) $language['id']) ?>">
                                            <label class="form-check-label" for="challengeLanguage<?= e((string) $language['id']) ?>">
                                                <?= e($language['name']) ?>
                                                <?php if (!(int) $language['is_active']): ?>
                                                    <span class="text-body-secondary">(inactivo)</span>
                                                <?php endif; ?>
                                            </label>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        </div>
                        <div class="tab-pane fade edit-only" id="challenge-github">
                            <label class="form-label" for="challengeGithubLinks">Enlaces de GitHub</label>
                            <textarea class="form-control" name="github_links" id="challengeGithubLinks" rows="5" placeholder="Un enlace por línea"></textarea>
                        </div>
                        <div class="tab-pane fade edit-only" id="challenge-notes">
                            <label class="form-label" for="challengeNotes">Notas</label>
                            <textarea class="form-control" name="notes" id="challengeNotes" rows="6"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="me-auto d-flex flex-wrap gap-2 edit-actions">
                        <button type="button" class="btn btn-success" id="challengeCompleteButton">Cumplido</button>
                        <button type="button" class="btn btn-outline-danger" id="challengeMissButton">No cumplido</button>
                        <button type="button" class="btn btn-outline-secondary" id="challengeCancelButton">Cancelar reto</button>
                    </div>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                    <button type="submit" class="btn btn-primary" id="challengeSubmitButton">Guardar reto</button>
                </div>
            </form>
        </div>
    </div>
</div>
