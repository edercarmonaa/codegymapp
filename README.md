# CodeGymApp

Aplicación web privada en PHP 8.3 para controlar retos de programación, calendario, rutinas, metas, reportes, notificaciones internas y bitácora de seguridad.

El proyecto está pensado para un hosting tradicional con cPanel, Apache, `.htaccess`, PHP 8.3 y MySQL. No usa Composer ni frameworks.

## Requisitos

- Apache con `mod_rewrite` y soporte para `.htaccess`.
- PHP 8.3 con PDO MySQL habilitado.
- MySQL 8 o MariaDB reciente con InnoDB.
- Subdominio o carpeta pública donde `index.php` quede en la raíz.
- HTTPS recomendado para que la cookie JWT use `Secure`.

## Instalación En cPanel

1. Clona o sube el repositorio a la carpeta del subdominio.
2. Verifica que `index.php`, `.htaccess`, `app`, `database`, `public`, `routes`, `storage` y `tools` queden en la misma raíz.
3. Crea una base de datos MySQL desde cPanel.
4. Crea un usuario MySQL y asígnalo a la base de datos con todos los permisos necesarios.
5. Importa `database/install.sql` desde phpMyAdmin sobre una base de datos vacía.
6. Copia `.env.example` como `.env`.
7. Edita `.env` con `APP_URL`, `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASS` y `JWT_SECRET`.
8. Abre `https://tu-subdominio/tools/create_user.php` y crea el usuario inicial.
9. Confirma que `tools/create_user.php` se borre automáticamente. Si no se borra, elimínalo manualmente.
10. Entra a `/login`.
11. Revisa `/dashboard`, `/calendario`, `/retos`, `/reportes`, `/notificaciones` y `/seguridad`.

## JWT_SECRET

Usa una clave larga y aleatoria. Ejemplo con terminal:

```bash
openssl rand -hex 32
```

Colócala en `.env`:

```env
JWT_SECRET=pega_aqui_la_clave_generada
```

## Archivo .env

Ejemplo:

```env
APP_NAME="CodeGymApp"
APP_ENV=production
APP_DEBUG=false
APP_URL=https://subdominio.tudominio.com

DB_HOST=localhost
DB_NAME=nombre_bd
DB_USER=usuario_bd
DB_PASS=password_bd
DB_CHARSET=utf8mb4

JWT_SECRET=clave_secreta_larga
JWT_EXPIRES_MINUTES=30

LOGIN_MAX_ATTEMPTS=3
LOGIN_BLOCK_MINUTES=30

RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS=120
RATE_LIMIT_WINDOW_SECONDS=60
```

Nunca subas `.env` a GitHub.

## Usuario Inicial

El sistema permite un solo usuario. El usuario inicial se crea con:

```text
/tools/create_user.php
```

El script:

- lee la conexión desde `.env`;
- valida la política de contraseña;
- crea el hash con `password_hash()`;
- impide crear más de un usuario;
- se borra automáticamente al terminar correctamente.

La contraseña debe tener mínimo 10 caracteres, mayúscula, minúscula, número y símbolo.

## Módulos

- Login/logout con JWT en cookie `HttpOnly`.
- Bloqueo por intentos fallidos.
- Bitácora de seguridad.
- Modo claro/oscuro.
- Dashboard con métricas y gráficas.
- Calendario con FullCalendar.
- Rutinas repetitivas diarias, semanales y mensuales.
- Registro y edición de retos calendarizados.
- Registro manual de retos ya realizados.
- Plataformas y lenguajes.
- Metas semanales, mensuales y anuales.
- Reportes con filtros y Chart.js.
- Notificaciones internas.
- Tablas con filtros, ordenamiento, paginación y actualización parcial con HTMX.

## Arquitectura PHP

El arranque en `app/core/bootstrap.php` usa un autoload compatible con PSR-4 para migraciones graduales:

- `CodeGymApp\Core\` apunta a `app/core/`
- `CodeGymApp\Controllers\` apunta a `app/controllers/`
- `CodeGymApp\Models\` apunta a `app/models/`
- `CodeGymApp\Helpers\` apunta a `app/helpers/`

El autoload mantiene compatibilidad con las clases actuales sin namespace para no romper despliegues en cPanel. Las nuevas clases pueden agregarse con namespace siguiendo esas rutas. `composer.json` declara el mismo mapeo para herramientas modernas, pero el proyecto no requiere dependencias Composer para ejecutarse.

## Seguridad

`.htaccess` bloquea acceso directo a:

- `.env`
- logs
- `/app`
- `/routes`
- `/database`
- `/storage`
- archivos internos de `/tools`, excepto `create_user.php` durante la instalación

Después de crear el usuario inicial, `tools/create_user.php` debe desaparecer.

### Mitigación De Ráfagas

La app incluye un límite ligero por IP antes de consultar MySQL. Por defecto permite `120` peticiones por `60` segundos y responde `429 Too Many Requests` cuando se supera.

Puedes ajustarlo en `.env`:

```env
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS=120
RATE_LIMIT_WINDOW_SECONDS=60
```

Para ataques volumétricos, usa también una capa externa como Cloudflare/WAF, ModSecurity de cPanel o reglas del firewall del hosting. El límite interno ayuda, pero no sustituye una protección antes de Apache/PHP.

## Reinstalación O Respaldo

Para reinstalar desde cero:

1. Respalda el código y la base de datos actual.
2. Crea una base de datos vacía.
3. Importa `database/install.sql`.
4. Crea `.env` desde `.env.example`.
5. Ejecuta `tools/create_user.php`.
6. Verifica login y módulos principales.

Para respaldo de producción:

- Exporta la base de datos desde phpMyAdmin.
- Respalda `.env` fuera del repositorio.
- Respalda el repositorio/carpeta del subdominio.

## Limpieza De Datos De Prueba

Si necesitas dejar la instalación lista para uso real sin borrar el usuario ni los catálogos base, ejecuta `database/cleanup_test_data.sql` desde phpMyAdmin.

El script conserva:

- `users`
- `platforms`
- `languages`

El script borra:

- retos y sus lenguajes/enlaces de GitHub;
- rutinas repetitivas;
- metas;
- notificaciones;
- bitácora de seguridad.

Antes de ejecutarlo, exporta un respaldo completo de la base de datos.

## Checklist Post-Instalación

- `/login` carga correctamente.
- El usuario inicial fue creado.
- `tools/create_user.php` ya no existe.
- `/dashboard` carga sin errores.
- `/calendario` muestra eventos y permite rutinas.
- `/retos` pagina y filtra.
- `/reportes` muestra gráficas.
- `/notificaciones` muestra historial.
- `/seguridad` registra eventos.
- `.env` no es accesible desde navegador.
- `/app`, `/database`, `/routes` y `/storage` devuelven acceso denegado.

## Solución De Problemas

- Error de conexión: revisa `DB_HOST`, `DB_NAME`, `DB_USER` y `DB_PASS` en `.env`.
- Pantalla en blanco: activa temporalmente `APP_DEBUG=true` y vuelve a cargar.
- Login expira rápido: revisa `JWT_EXPIRES_MINUTES`.
- CSS/JS no cargan: confirma que `/public/assets` sea accesible.
- Rutas no funcionan: confirma que `.htaccess` esté activo y que Apache permita `AllowOverride`.
