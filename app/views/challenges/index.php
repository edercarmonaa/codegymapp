<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Retos</h1>
        <p class="text-body-secondary mb-0">Historial y seguimiento de retos registrados</p>
    </div>
</div>

<div class="table-responsive">
    <table class="table align-middle">
        <thead>
            <tr>
                <th>Fecha programada</th>
                <th>Plataforma</th>
                <th>Nombre del reto</th>
                <th>Estado</th>
                <th>Dificultad</th>
                <th>Tiempo</th>
                <th>Fecha de cumplimiento</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($challenges as $challenge): ?>
                <tr>
                    <td><?= e($challenge['scheduled_date']) ?></td>
                    <td><?= e($challenge['platform_name']) ?></td>
                    <td><?= e($challenge['title'] ?: 'Pendiente por detallar') ?></td>
                    <td><span class="badge text-bg-secondary"><?= e($challenge['status']) ?></span></td>
                    <td><?= e($challenge['difficulty']) ?></td>
                    <td><?= e((string) $challenge['time_spent_minutes']) ?> min</td>
                    <td><?= e($challenge['completed_date']) ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$challenges): ?>
                <tr><td colspan="7" class="text-body-secondary">Todavía no hay retos registrados.</td></tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

