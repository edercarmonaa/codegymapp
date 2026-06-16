<?php

declare(strict_types=1);

final class ApiCatalogController
{
    public function platforms(): void
    {
        Response::json([
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], Platform::all()),
        ]);
    }

    public function activePlatforms(): void
    {
        Response::json([
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], Platform::active()),
        ]);
    }

    public function languages(): void
    {
        Response::json([
            'ok' => true,
            'languages' => array_map([$this, 'languageResource'], Language::all()),
        ]);
    }

    public function activeLanguages(): void
    {
        Response::json([
            'ok' => true,
            'languages' => array_map([$this, 'languageResource'], Language::active()),
        ]);
    }

    /** @param array<string, mixed> $platform @return array<string, mixed> */
    private function platformResource(array $platform): array
    {
        return [
            'id' => (int) ($platform['id'] ?? 0),
            'name' => (string) ($platform['name'] ?? ''),
            'description' => (string) ($platform['description'] ?? ''),
            'url' => safe_url($platform['url'] ?? null),
            'is_active' => (int) ($platform['is_active'] ?? 0) === 1,
        ];
    }

    /** @param array<string, mixed> $language @return array<string, mixed> */
    private function languageResource(array $language): array
    {
        return [
            'id' => (int) ($language['id'] ?? 0),
            'name' => (string) ($language['name'] ?? ''),
            'is_active' => (int) ($language['is_active'] ?? 0) === 1,
        ];
    }
}
