<?php
$pageTitle = $title ?? 'CodeGymApp';
$currentUser = Auth::user();
$publicTheme = $currentUser['preferred_theme'] ?? ($_COOKIE['codegym_public_theme'] ?? 'light');
if (!in_array($publicTheme, ['light', 'dark'], true)) {
    $publicTheme = 'light';
}
?>
<!doctype html>
<html lang="es" data-bs-theme="<?= e((string) $publicTheme) ?>">
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
<body>
    <?= $content ?>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/public/assets/js/app.js?v=4"></script>
    <script>
        (() => {
            const button = document.querySelector('[data-public-theme-toggle]');
            if (!button) {
                return;
            }

            const icon = button.querySelector('i');
            const render = () => {
                const theme = document.documentElement.getAttribute('data-bs-theme') || 'light';
                button.setAttribute('aria-label', theme === 'dark' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
                button.setAttribute('title', theme === 'dark' ? 'Tema claro' : 'Tema oscuro');
                if (icon) {
                    icon.className = theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
                }
            };

            button.addEventListener('click', () => {
                const current = document.documentElement.getAttribute('data-bs-theme') || 'light';
                const next = current === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-bs-theme', next);
                document.cookie = `codegym_public_theme=${next}; path=/; max-age=31536000; samesite=lax`;
                render();
            });

            render();
        })();
    </script>
</body>
</html>
