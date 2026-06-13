<?php if (!empty($_SESSION['flash_error']) || !empty($_SESSION['flash_success'])): ?>
    <div class="alert <?= !empty($_SESSION['flash_error']) ? 'alert-danger' : 'alert-success' ?> alert-dismissible fade show" role="alert">
        <?= e((string) ($_SESSION['flash_error'] ?? $_SESSION['flash_success'])) ?>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
    <?php unset($_SESSION['flash_error'], $_SESSION['flash_success']); ?>
<?php endif; ?>

