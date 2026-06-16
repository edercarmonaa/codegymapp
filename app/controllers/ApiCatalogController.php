<?php

declare(strict_types=1);

use CodeGymApp\Services\LanguageService;
use CodeGymApp\Services\PlatformService;

final class ApiCatalogController
{
    public function __construct(
        private readonly PlatformService $platformService = new PlatformService(),
        private readonly LanguageService $languageService = new LanguageService()
    ) {
    }

    public function platformList(): void
    {
        $data = $this->platformService->indexPayload();
        Response::json([
            'ok' => true,
            'platforms' => array_map([$this, 'platformResource'], $data['platforms']),
            'pagination' => $data['pagination'],
        ]);
    }

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

    public function languageList(): void
    {
        $data = $this->languageService->indexPayload();
        Response::json([
            'ok' => true,
            'languages' => array_map([$this, 'languageResource'], $data['languages']),
            'pagination' => $data['pagination'],
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
            'created_at' => (string) ($platform['created_at'] ?? ''),
        ];
    }

    /** @param array<string, mixed> $language @return array<string, mixed> */
    private function languageResource(array $language): array
    {
        return [
            'id' => (int) ($language['id'] ?? 0),
            'name' => (string) ($language['name'] ?? ''),
            'is_active' => (int) ($language['is_active'] ?? 0) === 1,
            'created_at' => (string) ($language['created_at'] ?? ''),
        ];
    }
}
