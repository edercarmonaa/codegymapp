<?php
$currentUser = Auth::user();
$theme = $currentUser['preferred_theme'] ?? 'light';
$pageTitle = $title ?? 'CodeGymApp';
?>
<!doctype html>
<html lang="es" data-bs-theme="<?= e((string) $theme) ?>">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e($pageTitle) ?> | CodeGymApp</title>
    <link rel="icon" type="image/png" href="/public/assets/img/site-icon.png">
    <link rel="apple-touch-icon" href="/public/assets/img/site-icon.png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="/public/assets/css/app.css?v=2" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.9.12"></script>
</head>
<body>
    <?php require __DIR__ . '/../partials/navbar.php'; ?>
    <main class="container-fluid app-shell py-4">
        <?php require __DIR__ . '/../partials/toast_container.php'; ?>
        <?= $content ?>
    </main>
    <?php require __DIR__ . '/../partials/confirm_modal.php'; ?>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
    <script src="/public/assets/js/app.js"></script>
</body>
</html>
