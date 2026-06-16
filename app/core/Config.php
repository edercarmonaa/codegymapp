<?php

declare(strict_types=1);

namespace CodeGymApp\Core;

use CodeGymApp\Core\Exceptions\ConfigurationException;

final class Config
{
    public static function validate(): void
    {
        $missing = [];
        foreach (['APP_URL', 'DB_HOST', 'DB_NAME', 'DB_USER', 'DB_PASS', 'JWT_SECRET'] as $key) {
            if (trim((string) Env::get($key, '')) === '') {
                $missing[] = $key;
            }
        }

        $jwtSecret = (string) Env::get('JWT_SECRET', '');
        if (
            str_starts_with($jwtSecret, 'pega_aqui')
            || $jwtSecret === 'clave_secreta_larga'
            || strlen($jwtSecret) < 32
        ) {
            $missing[] = 'JWT_SECRET valido de minimo 32 caracteres';
        }

        if ($missing !== []) {
            $message = 'Configuracion incompleta: ' . implode(', ', array_unique($missing)) . '. Revisa el archivo .env.';
            throw new ConfigurationException($message);
        }
    }
}

if (!\class_exists('Config', false)) {
    \class_alias(Config::class, 'Config');
}
