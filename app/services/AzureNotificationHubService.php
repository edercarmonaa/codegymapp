<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class AzureNotificationHubService
{
    private ?string $lastError = null;

    /** @param array<string, mixed> $user @param array<string, mixed> $device */
    public function registerInstallation(array $user, array $device): bool
    {
        $this->lastError = null;

        if (!\Env::get('NOTIFICATION_HUB_ENABLED', false)) {
            return true;
        }

        $connection = $this->connectionParts((string) \Env::get('NOTIFICATION_HUB_CONNECTION_STRING', ''));
        $hubName = trim((string) \Env::get('NOTIFICATION_HUB_NAME', ''));
        $token = trim((string) ($device['token'] ?? ''));

        if ($hubName === '' || $token === '' || !$connection) {
            $this->lastError = 'Falta NOTIFICATION_HUB_NAME, token o NOTIFICATION_HUB_CONNECTION_STRING no es válida.';
            \SecurityLog::record((int) ($user['id'] ?? 0), 'notification_hub_config_missing', 'warning', 'Azure Notification Hub no está configurado.');
            return false;
        }

        $installationId = $this->installationId($token);
        $endpoint = rtrim($connection['endpoint'], '/') . '/' . rawurlencode($hubName) . '/installations/' . rawurlencode($installationId);
        $url = $endpoint . '?api-version=2015-01';
        $payload = [
            'installationId' => $installationId,
            'platform' => (string) \Env::get('NOTIFICATION_HUB_PLATFORM', 'fcmv1'),
            'pushChannel' => $token,
            'tags' => [
                'user:' . (int) ($user['id'] ?? 0),
                'platform:android',
            ],
        ];

        return $this->putJson($url, $payload, $this->sasToken($endpoint, $connection));
    }

    /** @param array<string, string> $data */
    public function sendToUser(int $userId, string $title, string $message, array $data = []): bool
    {
        $this->lastError = null;

        if ($userId <= 0) {
            $this->lastError = 'Usuario no válido.';
            return false;
        }

        if (!\Env::get('NOTIFICATION_HUB_ENABLED', false)) {
            $this->lastError = 'Azure Notification Hub está deshabilitado en NOTIFICATION_HUB_ENABLED.';
            return false;
        }

        $connection = $this->connectionParts((string) \Env::get('NOTIFICATION_HUB_CONNECTION_STRING', ''));
        $hubName = trim((string) \Env::get('NOTIFICATION_HUB_NAME', ''));

        if ($hubName === '' || !$connection) {
            $this->lastError = 'Falta NOTIFICATION_HUB_NAME o NOTIFICATION_HUB_CONNECTION_STRING no es válida.';
            \SecurityLog::record($userId, 'notification_hub_config_missing', 'warning', 'Azure Notification Hub no está configurado.');
            return false;
        }

        $endpoint = rtrim($connection['endpoint'], '/') . '/' . rawurlencode($hubName) . '/messages/';
        $url = $endpoint . '?api-version=2015-01';
        $payload = [
            'message' => [
                'notification' => [
                    'title' => $this->truncate($title, 80),
                    'body' => $this->truncate($message, 180),
                ],
                'data' => $this->cleanData($data),
            ],
        ];

        return $this->postJson(
            $url,
            $payload,
            $this->sasToken($endpoint, $connection),
            [
                'ServiceBusNotification-Format: ' . (string) \Env::get('NOTIFICATION_HUB_SEND_FORMAT', 'fcmv1'),
                'ServiceBusNotification-Tags: user:' . $userId,
            ],
            $userId
        );
    }

    public function lastError(): ?string
    {
        return $this->lastError;
    }

    /** @return array{endpoint: string, keyName: string, key: string}|null */
    private function connectionParts(string $connection): ?array
    {
        $parts = [];
        foreach (explode(';', $connection) as $segment) {
            if (!str_contains($segment, '=')) {
                continue;
            }
            [$key, $value] = explode('=', $segment, 2);
            $parts[$key] = $value;
        }

        if (empty($parts['Endpoint']) || empty($parts['SharedAccessKeyName']) || empty($parts['SharedAccessKey'])) {
            return null;
        }

        return [
            'endpoint' => str_replace('sb://', 'https://', (string) $parts['Endpoint']),
            'keyName' => (string) $parts['SharedAccessKeyName'],
            'key' => (string) $parts['SharedAccessKey'],
        ];
    }

    /** @param array{keyName: string, key: string} $connection */
    private function sasToken(string $resourceUri, array $connection): string
    {
        $targetUri = strtolower(rawurlencode(strtolower($resourceUri)));
        $expires = time() + 3600;
        $signature = rawurlencode(base64_encode(hash_hmac('sha256', $targetUri . "\n" . $expires, $connection['key'], true)));

        return 'SharedAccessSignature sr=' . $targetUri
            . '&sig=' . $signature
            . '&se=' . $expires
            . '&skn=' . rawurlencode($connection['keyName']);
    }

    private function installationId(string $token): string
    {
        return 'codegym-' . substr(hash('sha256', $token), 0, 48);
    }

    /** @param array<string, string> $data @return array<string, string> */
    private function cleanData(array $data): array
    {
        $clean = [];
        foreach ($data as $key => $value) {
            $key = $this->truncate((string) $key, 40);
            if ($key === '') {
                continue;
            }

            $clean[$key] = $this->truncate((string) $value, 120);
        }

        return $clean;
    }

    private function truncate(string $value, int $maxLength): string
    {
        return substr(trim($value), 0, $maxLength);
    }

    /** @param array<string, mixed> $payload */
    private function putJson(string $url, array $payload, string $authorization): bool
    {
        if (!function_exists('curl_init')) {
            $this->lastError = 'cURL no está disponible en PHP.';
            \SecurityLog::record(null, 'notification_hub_curl_missing', 'warning', 'cURL no está disponible para registrar Azure Notification Hub.');
            return false;
        }

        $ch = curl_init($url);
        if ($ch === false) {
            $this->lastError = 'No se pudo inicializar cURL.';
            return false;
        }

        curl_setopt_array($ch, [
            CURLOPT_CUSTOMREQUEST => 'PUT',
            CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE),
            CURLOPT_HTTPHEADER => [
                'Authorization: ' . $authorization,
                'Content-Type: application/json',
                'x-ms-version: 2015-01',
            ],
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT => 10,
        ]);

        $response = (string) curl_exec($ch);
        $status = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
        $error = curl_error($ch);
        curl_close($ch);

        if ($status >= 200 && $status < 300) {
            return true;
        }

        $detail = $error !== '' ? $error : substr($response, 0, 180);
        $this->lastError = 'Azure respondió HTTP ' . $status . ($detail !== '' ? ': ' . $detail : '.');
        \SecurityLog::record(null, 'notification_hub_register_failed', 'warning', 'Azure Notification Hub respondió ' . $status . ($detail !== '' ? ': ' . $detail : '.'));
        return false;
    }

    /** @param array<string, mixed> $payload @param list<string> $extraHeaders */
    private function postJson(string $url, array $payload, string $authorization, array $extraHeaders, int $userId): bool
    {
        if (!function_exists('curl_init')) {
            $this->lastError = 'cURL no está disponible en PHP.';
            \SecurityLog::record($userId, 'notification_hub_curl_missing', 'warning', 'cURL no está disponible para enviar Azure Notification Hub.');
            return false;
        }

        $ch = curl_init($url);
        if ($ch === false) {
            $this->lastError = 'No se pudo inicializar cURL.';
            return false;
        }

        curl_setopt_array($ch, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE),
            CURLOPT_HTTPHEADER => array_merge([
                'Authorization: ' . $authorization,
                'Content-Type: application/json',
                'x-ms-version: 2015-01',
            ], $extraHeaders),
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT => 10,
        ]);

        $response = (string) curl_exec($ch);
        $status = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
        $error = curl_error($ch);
        curl_close($ch);

        if ($status >= 200 && $status < 300) {
            return true;
        }

        $detail = $error !== '' ? $error : substr($response, 0, 180);
        $this->lastError = 'Azure respondió HTTP ' . $status . ($detail !== '' ? ': ' . $detail : '.');
        \SecurityLog::record($userId, 'notification_hub_send_failed', 'warning', 'Azure Notification Hub respondió ' . $status . ($detail !== '' ? ': ' . $detail : '.'));
        return false;
    }
}
