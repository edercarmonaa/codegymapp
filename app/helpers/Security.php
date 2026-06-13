<?php

declare(strict_types=1);

function e(?string $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}

function csrf_token(): string
{
    if (empty($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }

    return (string) $_SESSION['csrf_token'];
}

function csrf_field(): string
{
    return '<input type="hidden" name="_token" value="' . e(csrf_token()) . '">';
}

function verify_csrf(): void
{
    $token = $_POST['_token'] ?? '';
    if (!is_string($token) || !hash_equals(csrf_token(), $token)) {
        http_response_code(419);
        exit('Token de seguridad inválido.');
    }
}

function password_policy_errors(string $password): array
{
    $errors = [];
    if (strlen($password) < 10) {
        $errors[] = 'La contraseña debe tener al menos 10 caracteres.';
    }
    if (!preg_match('/[A-Z]/', $password)) {
        $errors[] = 'La contraseña debe incluir al menos una mayúscula.';
    }
    if (!preg_match('/[a-z]/', $password)) {
        $errors[] = 'La contraseña debe incluir al menos una minúscula.';
    }
    if (!preg_match('/[0-9]/', $password)) {
        $errors[] = 'La contraseña debe incluir al menos un número.';
    }
    if (!preg_match('/[^A-Za-z0-9]/', $password)) {
        $errors[] = 'La contraseña debe incluir al menos un símbolo.';
    }
    return $errors;
}

