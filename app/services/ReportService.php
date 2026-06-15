<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class ReportService
{
    public function refreshReportData(): void
    {
        \Challenge::expirePending();
    }

    /**
     * @param array<string, mixed> $query
     * @return array<string, mixed>
     */
    public function reportPayload(array $query): array
    {
        $filters = $this->filtersFromQuery($query);

        return [
            'reports' => \Challenge::reportData($filters),
            'filters' => $filters,
            'platforms' => \Platform::all(),
            'languages' => \Language::all(),
            'statusLabels' => \Challenge::statusLabels(),
        ];
    }

    /**
     * @param array<string, mixed> $query
     * @return array{date_from: string, date_to: string, platform_id: int, language_id: int, status: string, completion_type: string}
     */
    private function filtersFromQuery(array $query): array
    {
        return [
            'date_from' => substr((string) ($query['date_from'] ?? ''), 0, 10),
            'date_to' => substr((string) ($query['date_to'] ?? ''), 0, 10),
            'platform_id' => max(0, (int) ($query['platform_id'] ?? 0)),
            'language_id' => max(0, (int) ($query['language_id'] ?? 0)),
            'status' => substr((string) ($query['status'] ?? ''), 0, 30),
            'completion_type' => substr((string) ($query['completion_type'] ?? ''), 0, 30),
        ];
    }
}
