<?php
$pageTitle = $title ?? 'CodeGymApp';
$authTheme = $_COOKIE['codegym_public_theme'] ?? 'light';
$authTheme = in_array($authTheme, ['light', 'dark'], true) ? $authTheme : 'light';
?>
<!doctype html>
<html lang="es" data-bs-theme="<?= e($authTheme) ?>">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e($pageTitle) ?> | CodeGymApp</title>
    <link rel="icon" type="image/png" href="/public/assets/img/site-icon.png">
    <link rel="apple-touch-icon" href="/public/assets/img/site-icon.png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="/public/assets/css/app.css?v=2" rel="stylesheet">
</head>
<body class="auth-body">
    <div class="position-fixed top-0 end-0 p-3">
        <button class="btn btn-outline-secondary btn-sm" type="button" data-auth-theme-toggle>
            <i class="bi <?= $authTheme === 'dark' ? 'bi-sun' : 'bi-moon-stars' ?>"></i>
        </button>
    </div>
    <main class="container min-vh-100 d-flex align-items-center justify-content-center">
        <?= $content ?>
    </main>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        (() => {
            const button = document.querySelector('[data-auth-theme-toggle]');
            const icon = button?.querySelector('i');
            const sync = () => {
                const theme = document.documentElement.getAttribute('data-bs-theme') || 'light';
                if (icon) icon.className = theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
                if (button) {
                    button.setAttribute('title', theme === 'dark' ? 'Tema claro' : 'Tema oscuro');
                    button.setAttribute('aria-label', theme === 'dark' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
                }
            };
            button?.addEventListener('click', () => {
                const current = document.documentElement.getAttribute('data-bs-theme') || 'light';
                const next = current === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-bs-theme', next);
                document.cookie = `codegym_public_theme=${next}; path=/; max-age=31536000; samesite=lax`;
                sync();
            });
            sync();
        })();
    </script>
</body>
</html>
