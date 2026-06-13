<?php

declare(strict_types=1);

final class TableState
{
    /** @param array<int, string> $allowedSorts @return array<string, mixed> */
    public static function fromRequest(array $allowedSorts, string $defaultSort = 'created_at', string $defaultDir = 'desc'): array
    {
        $perPage = (int) ($_GET['per_page'] ?? 20);
        if (!in_array($perPage, [10, 20, 25, 50], true)) {
            $perPage = 20;
        }

        $page = max(1, (int) ($_GET['page'] ?? 1));
        $sort = (string) ($_GET['sort'] ?? $defaultSort);
        if (!in_array($sort, $allowedSorts, true)) {
            $sort = $defaultSort;
        }

        $dir = strtolower((string) ($_GET['dir'] ?? $defaultDir));
        if (!in_array($dir, ['asc', 'desc'], true)) {
            $dir = $defaultDir;
        }

        return [
            'page' => $page,
            'per_page' => $perPage,
            'offset' => ($page - 1) * $perPage,
            'sort' => $sort,
            'dir' => $dir,
        ];
    }

    /** @param array<string, mixed> $state @return array<string, mixed> */
    public static function pagination(array $state, int $total): array
    {
        return [
            'page' => (int) $state['page'],
            'per_page' => (int) $state['per_page'],
            'total' => $total,
            'pages' => max(1, (int) ceil($total / max(1, (int) $state['per_page']))),
            'sort' => (string) $state['sort'],
            'dir' => (string) $state['dir'],
        ];
    }
}
