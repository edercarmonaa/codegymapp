<?php $pageTitle = $title ?? 'CodeGymApp'; ?>
<!doctype html>
<html lang="es" data-bs-theme="light">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e($pageTitle) ?> | CodeGymApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/public/assets/css/app.css" rel="stylesheet">
</head>
<body class="auth-body">
    <main class="container min-vh-100 d-flex align-items-center justify-content-center">
        <?= $content ?>
    </main>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

