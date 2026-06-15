<nav class="navbar navbar-expand-lg border-bottom bg-body">
    <div class="container-fluid">
        <a class="navbar-brand fw-semibold d-inline-flex align-items-center gap-2" href="/dashboard">
            <img class="navbar-logo" src="/public/assets/img/site-icon.png" width="28" height="28" alt="" aria-hidden="true">
            <span>CodeGymApp</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar" aria-controls="mainNavbar" aria-expanded="false" aria-label="Abrir navegación">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
<?php $unreadNotifications = Notification::unreadCount(); ?>
<?php foreach ([
                    '/dashboard' => 'Dashboard',
                    '/calendario' => 'Calendario',
                    '/retos' => 'Retos',
                    '/plataformas' => 'Plataformas',
                    '/lenguajes' => 'Lenguajes',
                    '/metas' => 'Metas',
                    '/notificaciones' => 'Notificaciones',
                    '/usuario' => 'Mi usuario',
                    '/seguridad' => 'Seguridad',
                ] as $href => $label): ?>
                    <li class="nav-item">
                        <a class="nav-link" href="<?= e($href) ?>">
                            <?= e($label) ?>
                            <?php if ($href === '/notificaciones' && $unreadNotifications > 0): ?>
                                <span class="badge rounded-pill text-bg-danger"><?= e((string) $unreadNotifications) ?></span>
                            <?php endif; ?>
                        </a>
                    </li>
                <?php endforeach; ?>
            </ul>
            <form class="d-flex align-items-center gap-2 me-3" action="/usuario/cambiar-tema" method="post">
                <?= csrf_field() ?>
                <input type="hidden" name="theme" value="<?= ($theme ?? 'light') === 'dark' ? 'light' : 'dark' ?>">
                <button class="btn btn-outline-secondary btn-sm" title="Cambiar tema" aria-label="Cambiar tema">
                    <i class="bi <?= ($theme ?? 'light') === 'dark' ? 'bi-sun' : 'bi-moon-stars' ?>"></i>
                </button>
            </form>
            <form action="/logout" method="post">
                <?= csrf_field() ?>
                <button class="btn btn-outline-danger btn-sm">Cerrar sesión</button>
            </form>
        </div>
    </div>
</nav>
