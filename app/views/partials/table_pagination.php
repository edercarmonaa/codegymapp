<?php
$pagination = $pagination ?? ['page' => 1, 'pages' => 1, 'per_page' => 20, 'total' => 0];
$baseParams = $_GET;
unset($baseParams['page']);
$pageUrl = static function (int $page) use ($baseParams): string {
    return '?' . http_build_query(array_merge($baseParams, ['page' => $page]));
};
$perPageUrl = static function (int $perPage) use ($baseParams): string {
    return '?' . http_build_query(array_merge($baseParams, ['page' => 1, 'per_page' => $perPage]));
};
?>
<div class="d-flex flex-wrap gap-2 align-items-center justify-content-between my-3">
    <div class="d-flex flex-wrap gap-3 align-items-center">
        <div class="text-body-secondary small">
            <?= e((string) $pagination['total']) ?> registros
        </div>
        <?php if (!empty($bulkTable ?? '') && !empty($bulkActions ?? [])): ?>
            <?php require __DIR__ . '/table_bulk_toolbar.php'; ?>
        <?php endif; ?>
    </div>
    <div class="d-flex flex-wrap gap-2 align-items-center">
        <label class="form-label mb-0 small">Por página</label>
        <select class="form-select form-select-sm w-auto table-per-page">
            <?php foreach ([10, 20, 25, 50] as $option): ?>
                <option value="<?= e($perPageUrl($option)) ?>" <?= (int) $pagination['per_page'] === $option ? 'selected' : '' ?>><?= e((string) $option) ?></option>
            <?php endforeach; ?>
        </select>
        <nav aria-label="Paginación">
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item <?= (int) $pagination['page'] <= 1 ? 'disabled' : '' ?>">
                    <a class="page-link" data-table-link="1" href="<?= e($pageUrl(max(1, (int) $pagination['page'] - 1))) ?>">Anterior</a>
                </li>
                <li class="page-item disabled">
                    <span class="page-link"><?= e((string) $pagination['page']) ?> / <?= e((string) $pagination['pages']) ?></span>
                </li>
                <li class="page-item <?= (int) $pagination['page'] >= (int) $pagination['pages'] ? 'disabled' : '' ?>">
                    <a class="page-link" data-table-link="1" href="<?= e($pageUrl(min((int) $pagination['pages'], (int) $pagination['page'] + 1))) ?>">Siguiente</a>
                </li>
            </ul>
        </nav>
    </div>
</div>
