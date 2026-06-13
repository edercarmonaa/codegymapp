# CodeGymApp

Aplicación web privada en PHP 8.3 para controlar retos de programación, calendario, metas, estadísticas, notificaciones internas y seguridad con JWT propio.

## Requisitos

- Apache con soporte para `.htaccess`
- PHP 8.3
- MySQL
- PDO MySQL habilitado
- Hosting tradicional tipo cPanel

No usa Composer ni frameworks.

## Instalación en cPanel

1. Clona el repositorio dentro de la carpeta del subdominio.
2. Copia `.env.example` como `.env`.
3. Crea una base de datos MySQL desde cPanel.
4. Crea un usuario MySQL y asígnalo a la base de datos.
5. Edita `.env` con los datos de conexión, `APP_URL` y `JWT_SECRET`.
6. Importa `database/install.sql` desde phpMyAdmin o la herramienta disponible en cPanel.
7. Ejecuta `tools/create_user.php` desde navegador o CLI.
8. Verifica que `tools/create_user.php` se borre automáticamente después de crear el usuario.
9. Entra a `/login`.
10. Cambia la contraseña inicial desde “Mi usuario”.

## Seguridad

- El login usa contraseña con `password_hash()` y `password_verify()`.
- La sesión usa JWT propio en cookie `HttpOnly`.
- La cookie usa `Secure` cuando el sitio está bajo HTTPS.
- El token caduca según `JWT_EXPIRES_MINUTES`.
- Los intentos fallidos se registran y pueden bloquear al usuario.
- `.htaccess` bloquea carpetas sensibles y archivos internos.

## Estructura

```text
/app
/config
/routes
/database
/tools
/public/assets
/storage
index.php
.htaccess
.env.example
```

## Módulos incluidos

- Login y logout
- Dashboard base con métricas y Chart.js
- Navbar superior
- Modo claro/oscuro guardado en usuario
- Catálogo de plataformas
- Catálogo de lenguajes
- Listado base de retos
- Calendario preparado con FullCalendar
- Pantallas base de metas, reportes, notificaciones, usuario y seguridad

## Pendiente por confirmar

- Completar endpoints JSON de calendario.
- Implementar rutinas, metas, reportes y notificaciones con reglas completas.
- Añadir paginación, filtros y HTMX parcial en tablas.
