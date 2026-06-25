# CodeGymApp

Aplicación web privada en PHP 8.3 para controlar retos de programación, calendario, rutinas, metas, reportes, notificaciones internas y bitácora de seguridad.

El proyecto está pensado para un hosting tradicional con cPanel, Apache, `.htaccess`, PHP 8.3 y MySQL. No usa frameworks. `composer.json` existe para describir el autoload PSR-4, pero la app puede ejecutarse sin instalar dependencias Composer.

## Ramas Del Repositorio

- `main`: aplicación móvil Android nativa en Kotlin.
- `hosting`: API PHP y frontend web que deben desplegarse en cPanel.

Cuando actualices el hosting desde cPanel, usa siempre la rama `hosting`. La rama `main` queda reservada para el proyecto móvil Android.

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
11. Revisa `/calendario`, `/dashboard`, `/retos`, `/notificaciones` y `/seguridad`.

## Despliegue En cPanel Desde Git

1. Entra a **Git Version Control**.
2. Abre el repositorio de `codegymapp`.
3. Confirma que la rama activa sea `hosting`.
4. Usa **Actualizar desde remoto**.
5. Usa **Desplegar commit HEAD**.
6. Si cambiaron CSS/JS, recarga el navegador con cache limpio.

Después del despliegue confirma que el commit mostrado en cPanel coincide con el último commit de `origin/hosting`.

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

NOTIFICATION_HUB_ENABLED=false
NOTIFICATION_HUB_NAME=
NOTIFICATION_HUB_CONNECTION_STRING=
NOTIFICATION_HUB_PLATFORM=fcmv1
NOTIFICATION_HUB_SEND_FORMAT=fcmv1
```

Nunca subas `.env` a GitHub.

## Azure Notification Hubs

La app móvil registra el token FCM del dispositivo en el backend. Si Azure Notification Hubs está habilitado, el backend también registra/actualiza la instalación en el hub usando tags:

- `user:{id}`
- `platform:android`

Configura estas variables en `.env`:

```env
NOTIFICATION_HUB_ENABLED=true
NOTIFICATION_HUB_NAME=nombre_del_hub
NOTIFICATION_HUB_CONNECTION_STRING="Endpoint=sb://...;SharedAccessKeyName=...;SharedAccessKey=..."
NOTIFICATION_HUB_PLATFORM=fcmv1
NOTIFICATION_HUB_SEND_FORMAT=fcmv1
```

Antes de habilitarlo, crea la tabla con `database/mobile_device_tokens.sql` si tu base ya existía antes de esta funcionalidad.

Si ya habías creado la tabla con una versión anterior, ajusta el tamaño del token:

```sql
ALTER TABLE mobile_device_tokens MODIFY token VARCHAR(512) NOT NULL;
```

Para validar el envío desde Postman, inicia sesión con `/api/auth/login`, copia el token y envía:

```http
POST /api/mobile/notifications/test
Authorization: Bearer TU_TOKEN
Content-Type: application/json
```

```json
{
  "title": "CodeGymApp",
  "message": "Prueba de notificación push"
}
```

## API Inicial

La API vive en el mismo dominio de la web usando rutas `/api/...`.

### Autenticación

La web conserva el login tradicional:

- `GET /login`: muestra formulario web.
- `POST /login`: valida credenciales, crea cookie JWT `HttpOnly`, `Secure`, `SameSite=Strict` y redirige a `/calendario`.

Android usa un endpoint JSON:

- `POST /api/auth/login`

Acepta JSON:

```json
{
  "username": "usuario",
  "password": "password"
}
```

También acepta `application/x-www-form-urlencoded` con los mismos campos.

Respuesta correcta:

```json
{
  "ok": true,
  "token": "JWT",
  "expires_in": 1800,
  "user": {
    "id": 1,
    "username": "usuario",
    "name": "Nombre",
    "email": "correo@dominio.com"
  }
}
```

Respuesta incorrecta:

```json
{
  "ok": false,
  "message": "Usuario o contraseña incorrectos."
}
```

### Validación Del Token

El backend valida un único JWT de 30 minutos de dos formas:

1. Primero busca `Authorization: Bearer <TOKEN>` para Android.
2. Si no existe, busca la cookie `codegymapp_token` para la web.

En Apache/cPanel, `.htaccess` preserva el header `Authorization` para que PHP pueda leerlo.

### Endpoints De Lectura

Todos estos endpoints requieren JWT por `Authorization: Bearer <TOKEN>` o cookie web activa:

- `GET /api/me`: usuario autenticado.
- `GET /api/catalogs/platforms/list`: plataformas paginadas para la tabla web.
- `GET /api/catalogs/platforms`: plataformas registradas.
- `GET /api/catalogs/platforms/active`: plataformas activas.
- `GET /api/catalogs/languages/list`: lenguajes paginados para la tabla web.
- `GET /api/catalogs/languages`: lenguajes registrados.
- `GET /api/catalogs/languages/active`: lenguajes activos.
- `GET /api/dashboard/summary`: resumen, métricas, rachas, atención, metas y datos de gráficas.
- `GET /api/mobile/today`: datos optimizados para la pantalla móvil Mi día, con retos pendientes de hoy y vencidos por revisar.
- `GET /api/mobile/planned`: retos pendientes futuros para la pantalla móvil Planeado.
- `GET /api/mobile/challenges`: retos del mes para la pantalla móvil Retos. Acepta `month=YYYY-MM` y `status=pending|completed|expired|missed|cancelled|all`.
- `GET /api/mobile/challenges/create-options`: plataformas activas para crear retos desde móvil.
- `GET /api/calendar/bootstrap`: datos base del calendario, plataformas, lenguajes y rutinas.
- `GET /api/calendar/routines`: rutinas registradas.
- `GET /api/calendar/events`: eventos en formato FullCalendar.
- `GET /api/challenges/list`: retos paginados con filtros de estado y plataforma.
- `GET /api/goals/list`: metas paginadas para la tabla web.
- `GET /api/notifications/list`: notificaciones paginadas para la tabla web.
- `GET /api/reports`: reportes con filtros opcionales.

### Endpoints Móviles De Escritura

Estos endpoints requieren JWT por `Authorization: Bearer <TOKEN>` y reciben JSON:

- `POST /api/mobile/challenges/store`: crea un reto desde móvil. Requiere `{"platform_id": 1, "scheduled_date": "YYYY-MM-DD"}`.
- `POST /api/mobile/challenges/complete`: marca un reto como cumplido. Requiere `{"id": 1}` y conserva las validaciones de datos completos del sistema web.
- `POST /api/mobile/challenges/miss`: marca un reto como no realizado. Requiere `{"id": 1}`.

Filtros aceptados por `/api/reports`:

- `date_from`
- `date_to`
- `platform_id`
- `language_id`
- `status`
- `completion_type`

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
- Dashboard con pestañas de datos generales, gráficas y reportes.
- Calendario con FullCalendar.
- Rutinas repetitivas diarias, semanales y mensuales.
- Registro y edición de retos calendarizados.
- Registro manual de retos ya realizados.
- Plataformas y lenguajes.
- Metas semanales, mensuales y anuales.
- Reportes con filtros y Chart.js integrados dentro de Dashboard.
- Notificaciones internas.
- Tablas con filtros, ordenamiento, paginación y actualización parcial con HTMX.

## Arquitectura PHP

El arranque en `app/core/bootstrap.php` usa un autoload compatible con PSR-4:

- `CodeGymApp\Core\` apunta a `app/core/`
- `CodeGymApp\Controllers\` apunta a `app/controllers/`
- `CodeGymApp\Models\` apunta a `app/models/`
- `CodeGymApp\Helpers\` apunta a `app/helpers/`
- `CodeGymApp\Services\` apunta a `app/services/`

El autoload mantiene compatibilidad con clases sin namespace para no romper despliegues en cPanel. Las clases nuevas deben crearse con namespace siguiendo esas rutas. `composer.json` declara el mismo mapeo para herramientas modernas.

### Capas Principales

- `index.php`: punto de entrada HTTP.
- `app/core/Application.php`: inicializa entorno, errores, rate limit, configuración y sesión.
- `app/core/Router.php`: resuelve ruta, método y controlador.
- `app/core/View.php`: renderiza vistas, layouts y partials.
- `app/controllers/`: reciben request, validan CSRF cuando aplica, delegan a servicios y responden.
- `app/services/`: concentran lógica de aplicación, validación y armado de payloads.
- `app/models/`: encapsulan consultas SQL y operaciones de persistencia.
- `app/views/`: HTML/PHP de presentación.

### Servicios Actuales

- `AuthService`: login, logout, bloqueo e intentos fallidos.
- `DashboardService`: métricas, rachas, listas y gráficas del dashboard.
- `ReportService`: filtros y payload de reportes.
- `CalendarService`: eventos, retos, rutinas y acciones del calendario.
- `CalendarPageService`: payload de la vista del calendario.
- `ChallengeService`: tabla de retos y registro manual.
- `GoalService`: metas, progreso y acciones.
- `PlatformService` / `LanguageService`: catálogos.
- `NotificationService`: generación, listado y acciones de notificaciones.
- `UserService`: perfil, contraseña y tema.
- `SecurityLogService`: bitácora de seguridad.

### Convenciones

- Archivos PHP con `declare(strict_types=1);`.
- Clases nuevas con namespace `CodeGymApp\...`.
- Controladores del router actual se mantienen sin namespace para compatibilidad.
- Validaciones de formularios en servicios/validadores, no en vistas.
- SQL en modelos; no agregar SQL directo en controladores.
- Redirecciones y flashes se manejan en controladores.
- Respuestas JSON de API se mantienen con `Response::json()`.

## Versión Android

La versión móvil Android se trabaja desde la rama `main`. La rama `hosting` se mantiene enfocada en API PHP y frontend web.

La primera etapa móvil debe consumir el API del mismo dominio mediante `Authorization: Bearer <TOKEN>`. El login móvil inicia con `POST /api/auth/login`.

Para trabajar Android:

1. Cambia a la rama `main`.
2. Abre la carpeta Android en Android Studio.
2. Deja que Android Studio sincronice Gradle.
3. Ejecuta `app` en un emulador o dispositivo físico.

La app Android no debe usar tráfico HTTP claro y debe guardar el JWT en almacenamiento seguro del dispositivo.

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
- Después de login se abre `/calendario`.
- `/dashboard` carga pestañas de datos generales, gráficas y reportes.
- `/calendario` muestra eventos y permite rutinas.
- `/retos` pagina y filtra.
- La pestaña Reportes muestra gráficas y filtros.
- `/notificaciones` muestra historial.
- `/seguridad` registra eventos.
- `.env` no es accesible desde navegador.
- `/app`, `/database`, `/routes` y `/storage` devuelven acceso denegado.

## Checklist De Regresión Antes De Producción

Ejecuta esta lista después de cambios grandes o refactors:

- Login correcto redirige a `/calendario`.
- Login incorrecto muestra error y registra evento en `/seguridad`.
- Logout regresa a `/login`.
- Dashboard abre las tres pestañas.
- Reportes filtra sin perder gráficas.
- Calendario carga eventos del mes.
- Crear reto desde calendario.
- Editar detalle de reto.
- Marcar reto como cumplido, no cumplido y cancelado.
- Crear rutina diaria, semanal y mensual.
- Editar rutina reduciendo días y confirmar que el calendario cambia.
- Desactivar rutina y confirmar que sus retos pendientes se cancelan.
- `/retos` filtra, ordena y pagina.
- Registro manual de reto valida campos obligatorios.
- Crear meta y desactivar meta.
- Crear, editar, activar y desactivar plataforma.
- Crear, editar, activar y desactivar lenguaje.
- Notificaciones: marcar leída y eliminar leída.
- Usuario: actualizar perfil, cambiar tema y cambiar contraseña.
- Seguridad: revisar que la bitácora liste eventos recientes.

## Checklist De Deploy

- Confirmar que estás en la rama local `hosting`.
- Confirmar que `git status` está limpio.
- Confirmar último commit en `origin/hosting`.
- En cPanel, confirmar rama activa `hosting`.
- Actualizar desde remoto.
- Desplegar commit HEAD.
- Recargar navegador con cache limpio si hubo cambios en `public/assets`.
- Revisar `/login`, `/calendario`, `/dashboard`, `/retos`, `/metas`, `/notificaciones` y `/seguridad`.

## Solución De Problemas

- Error de conexión: revisa `DB_HOST`, `DB_NAME`, `DB_USER` y `DB_PASS` en `.env`.
- Pantalla en blanco: activa temporalmente `APP_DEBUG=true` y vuelve a cargar.
- Login expira rápido: revisa `JWT_EXPIRES_MINUTES`.
- CSS/JS no cargan: confirma que `/public/assets` sea accesible.
- Rutas no funcionan: confirma que `.htaccess` esté activo y que Apache permita `AllowOverride`.
