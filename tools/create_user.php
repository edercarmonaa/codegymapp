<?php

declare(strict_types=1);

require_once __DIR__ . '/../app/core/bootstrap.php';

$isCli = PHP_SAPI === 'cli';

function output_message(string $message, bool $isCli): void
{
    if ($isCli) {
        echo $message . PHP_EOL;
        return;
    }
    echo '<p>' . e($message) . '</p>';
}

function form_page(array $errors = []): void
{
    ?>
    <!doctype html>
    <html lang="es">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Crear usuario | CodeGymApp</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="container py-5">
        <main class="mx-auto" style="max-width: 520px;">
            <h1 class="h3">Crear usuario inicial</h1>
            <?php foreach ($errors as $error): ?>
                <div class="alert alert-danger"><?= e($error) ?></div>
            <?php endforeach; ?>
            <form method="post" class="vstack gap-3">
                <div><label class="form-label">Nombre</label><input class="form-control" name="name" required></div>
                <div><label class="form-label">Usuario</label><input class="form-control" name="username" required></div>
                <div><label class="form-label">Correo</label><input class="form-control" type="email" name="email" required></div>
                <div><label class="form-label">Contraseña</label><input class="form-control" type="password" name="password" required></div>
                <button class="btn btn-primary">Crear usuario</button>
            </form>
        </main>
    </body>
    </html>
    <?php
}

function create_initial_user(string $name, string $username, string $email, string $password): void
{
    $sql = "INSERT INTO users (name, username, email, password_hash, preferred_theme, failed_login_attempts, is_active, created_at, updated_at)
        VALUES (:name, :username, :email, :password_hash, 'light', 0, 1, NOW(), NOW())";
    $payload = [
        'name' => $name,
        'username' => $username,
        'email' => $email,
        'password_hash' => password_hash($password, PASSWORD_DEFAULT),
    ];

    try {
        $stmt = Database::reconnect()->prepare($sql);
        $stmt->execute($payload);
    } catch (PDOException $exception) {
        $driverCode = (int) ($exception->errorInfo[1] ?? 0);
        if (!in_array($driverCode, [2006, 2013], true)) {
            throw $exception;
        }

        $stmt = Database::reconnect()->prepare($sql);
        $stmt->execute($payload);
    }
}

try {
    if (User::countAll() > 0) {
        output_message('Ya existe un usuario. No se permite crear otro.', $isCli);
        exit;
    }
} catch (Throwable $exception) {
    output_message('No se pudo consultar la tabla users. Importa el esquema confirmado antes de ejecutar este script.', $isCli);
    output_message($exception->getMessage(), $isCli);
    exit;
}

if ($isCli) {
    $name = trim((string) readline('Nombre: '));
    $username = trim((string) readline('Usuario: '));
    $email = trim((string) readline('Correo: '));
    $password = (string) readline('Contraseña: ');
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = trim((string) ($_POST['name'] ?? ''));
    $username = trim((string) ($_POST['username'] ?? ''));
    $email = trim((string) ($_POST['email'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');
} else {
    form_page();
    exit;
}

$errors = [];
if ($name === '') {
    $errors[] = 'El nombre es obligatorio.';
}
if ($username === '') {
    $errors[] = 'El usuario es obligatorio.';
}
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    $errors[] = 'El correo no es válido.';
}
$errors = array_merge($errors, password_policy_errors($password));

if ($errors) {
    if ($isCli) {
        foreach ($errors as $error) {
            output_message($error, true);
        }
    } else {
        form_page($errors);
    }
    exit;
}

create_initial_user($name, $username, $email, $password);

output_message('Usuario inicial creado correctamente.', $isCli);

if (@unlink(__FILE__)) {
    output_message('El script create_user.php se borró automáticamente.', $isCli);
} else {
    output_message('No se pudo borrar el script automáticamente. Elimínalo manualmente.', $isCli);
}
