<?php
$bulkActions = $bulkActions ?? [];
$bulkTable = $bulkTable ?? '';
?>
<?php if ($bulkTable && $bulkActions): ?>
    <div class="d-flex flex-wrap gap-2 align-items-center">
        <div class="form-check mb-0">
            <input class="form-check-input" type="checkbox" id="bulk_select_<?= e($bulkTable) ?>" data-bulk-select-all="<?= e($bulkTable) ?>">
            <label class="form-check-label small" for="bulk_select_<?= e($bulkTable) ?>">Seleccionar visibles</label>
        </div>
        <?php foreach ($bulkActions as $action): ?>
            <button
                class="btn btn-sm <?= e($action['class'] ?? 'btn-outline-secondary') ?>"
                type="button"
                data-bulk-action="<?= e($bulkTable) ?>"
                data-bulk-url="<?= e($action['url']) ?>"
                data-bulk-refresh="<?= e($action['refresh'] ?? $bulkTable) ?>"
                data-confirm="<?= e($action['confirm'] ?? '') ?>"
                disabled
            >
                <?= e($action['label']) ?>
            </button>
        <?php endforeach; ?>
    </div>
<?php endif; ?>
