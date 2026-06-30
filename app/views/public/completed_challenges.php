<?php
$sort = $sort ?? 'completed_date';
$dir = $dir ?? 'desc';
$sortUrl = static function (string $column) use ($sort, $dir): string {
    $nextDir = $sort === $column && $dir === 'asc' ? 'desc' : 'asc';
    return '?' . http_build_query(array_merge($_GET, ['sort' => $column, 'dir' => $nextDir, 'page' => 1]));
};
$sortIcon = static function (string $column) use ($sort, $dir): string {
    if ($sort !== $column) {
        return '<i class="bi bi-arrow-down-up ms-1 text-body-secondary"></i>';
    }

    return $dir === 'asc'
        ? '<i class="bi bi-sort-up ms-1"></i>'
        : '<i class="bi bi-sort-down ms-1"></i>';
};
?>

<nav class="navbar navbar-expand bg-body border-bottom sticky-top">
    <div class="container-fluid app-shell">
        <a class="navbar-brand d-flex align-items-center gap-2 fw-semibold" href="/">
            <img src="/public/assets/img/site-icon.png" alt="CodeGymApp" width="32" height="32" class="rounded">
            <span>CodeGymApp</span>
        </a>
        <div class="d-flex align-items-center gap-2">
            <button class="btn btn-outline-secondary btn-sm" type="button" data-public-theme-toggle>
                <i class="bi bi-moon-stars"></i>
            </button>
            <a class="btn btn-primary btn-sm" href="/login">Iniciar sesión</a>
        </div>
    </div>
</nav>

<main class="container-fluid app-shell py-4">
    <section class="rounded-4 border bg-body-tertiary p-4 p-lg-5 mb-4">
        <div class="row align-items-center g-4">
            <div class="col-lg-8">
                <span class="badge text-bg-success mb-3">Retos cumplidos</span>
                <h1 class="display-6 fw-semibold mb-3">Historial público de práctica</h1>
                <p class="lead text-body-secondary mb-0">
                    Un espacio donde publico  retos de programación para practicar lógica, resolver problemas y mejorar paso a paso mis habilidades como desarrollador.
                </p>
            </div>
        </div>
    </section>

    <section class="card shadow-sm border-0">
        <div class="card-body" id="tablePanel">
            <form class="row g-2 align-items-end mb-4" method="get" action="/">
                <div class="col-12 col-md-4 col-xl-3">
                    <label class="form-label" for="platform_id">Plataforma</label>
                    <select class="form-select" id="platform_id" name="platform_id">
                        <option value="0">Todas</option>
                        <?php foreach ($platforms as $platform): ?>
                            <option value="<?= e((string) $platform['id']) ?>" <?= (int) ($filters['platform_id'] ?? 0) === (int) $platform['id'] ? 'selected' : '' ?>>
                                <?= e($platform['name']) ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="col-12 col-md-4 col-xl-3">
                    <label class="form-label" for="language_id">Lenguaje</label>
                    <select class="form-select" id="language_id" name="language_id">
                        <option value="0">Todos</option>
                        <?php foreach ($languages as $language): ?>
                            <option value="<?= e((string) $language['id']) ?>" <?= (int) ($filters['language_id'] ?? 0) === (int) $language['id'] ? 'selected' : '' ?>>
                                <?= e($language['name']) ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="col-12 col-md-4 col-xl-2">
                    <label class="form-label" for="difficulty">Nivel</label>
                    <select class="form-select" id="difficulty" name="difficulty">
                        <option value="">Todos</option>
                        <?php foreach ($difficultyOptions as $value => $label): ?>
                            <option value="<?= e($value) ?>" <?= ($filters['difficulty'] ?? '') === $value ? 'selected' : '' ?>>
                                <?= e($label) ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="col-12 col-xl-auto">
                    <button class="btn btn-outline-primary">Filtrar</button>
                    <a class="btn btn-outline-secondary" data-table-link="1" href="/">Limpiar</a>
                </div>
            </form>

            <?php require __DIR__ . '/../partials/table_pagination.php'; ?>

            <div class="table-responsive">
                <table class="table align-middle table-hover">
                    <thead>
                        <tr>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('completed_date')) ?>">Fecha de publicación<?= $sortIcon('completed_date') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('platform')) ?>">Plataforma<?= $sortIcon('platform') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('title')) ?>">Nombre del reto<?= $sortIcon('title') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('difficulty')) ?>">Dificultad<?= $sortIcon('difficulty') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('languages')) ?>">Lenguajes<?= $sortIcon('languages') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('challenge_url')) ?>">Reto<?= $sortIcon('challenge_url') ?></a></th>
                            <th><a class="link-body-emphasis text-decoration-none" href="<?= e($sortUrl('github')) ?>">GitHub<?= $sortIcon('github') ?></a></th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($challenges as $challenge): ?>
                            <?php
                            $challengeUrl = safe_url($challenge['challenge_url'] ?? null);
                            $githubUrls = array_values(array_filter(array_map('safe_url', explode("\n", (string) ($challenge['github_urls'] ?? '')))));
                            ?>
                            <tr>
                                <td><?= e((string) ($challenge['completed_date'] ?: '-')) ?></td>
                                <td><?= e((string) $challenge['platform_name']) ?></td>
                                <td>
                                    <div class="fw-semibold"><?= e((string) ($challenge['title'] ?: 'Reto sin nombre')) ?></div>
                                </td>
                                <td><?= e((string) ($challenge['difficulty'] ?: '-')) ?></td>
                                <td><?= e((string) ($challenge['language_names'] ?: '-')) ?></td>
                                <td>
                                    <?php if ($challengeUrl): ?>
                                        <a href="<?= e($challengeUrl) ?>" target="_blank" rel="noopener">
                                            Ver reto
                                            <i class="bi bi-box-arrow-up-right ms-1"></i>
                                        </a>
                                    <?php else: ?>
                                        <span class="text-body-secondary">-</span>
                                    <?php endif; ?>
                                </td>
                                <td>
                                    <?php if ($githubUrls): ?>
                                        <div class="d-flex flex-column gap-1">
                                            <?php foreach ($githubUrls as $index => $url): ?>
                                                <a href="<?= e($url) ?>" target="_blank" rel="noopener">
                                                    GitHub <?= e((string) ($index + 1)) ?>
                                                </a>
                                            <?php endforeach; ?>
                                        </div>
                                    <?php else: ?>
                                        <span class="text-body-secondary">-</span>
                                    <?php endif; ?>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (!$challenges): ?>
                            <tr>
                                <td colspan="7" class="text-center text-body-secondary py-5">
                                    No hay retos cumplidos con esos filtros.
                                </td>
                            </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>

            <?php require __DIR__ . '/../partials/table_pagination.php'; ?>
        </div>
    </section>
</main>
