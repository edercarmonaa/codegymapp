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
                    <input type="hidden" name="scheduled_date" id="challengeScheduledDate">
                    <div id="challengeAlert" class="alert alert-danger d-none"></div>

                    <ul class="nav nav-tabs" role="tablist">
                        <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#challenge-data" type="button">Datos del reto</button></li>
                        <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-languages" type="button">Lenguajes</button></li>
                        <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-github" type="button">GitHub</button></li>
                        <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#challenge-notes" type="button">Notas</button></li>
                    </ul>
                    <div class="tab-content border border-top-0 p-3">
                        <div class="tab-pane fade show active" id="challenge-data">
                            <div class="row g-3">
                                <div class="col-12 col-md-6">
                                    <label class="form-label" for="challengePlatform">Plataforma</label>
                                    <select class="form-select" name="platform_id" id="challengePlatform" required>
                                        <option value="">Selecciona una plataforma</option>
                                        <?php foreach ($platforms as $platform): ?>
                                            <option value="<?= e((string) $platform['id']) ?>"><?= e($platform['name']) ?></option>
                                        <?php endforeach; ?>
                                    </select>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label">Fecha programada</label>
                                    <input class="form-control" id="challengeScheduledDateLabel" disabled>
                                </div>
                            </div>
                        </div>
                        <div class="tab-pane fade" id="challenge-languages">No hay lenguajes seleccionados.</div>
                        <div class="tab-pane fade" id="challenge-github">No hay enlaces de GitHub registrados.</div>
                        <div class="tab-pane fade" id="challenge-notes">No hay notas registradas.</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                    <button type="submit" class="btn btn-primary" id="challengeSubmitButton">Guardar reto</button>
                </div>
            </form>
        </div>
    </div>
</div>
