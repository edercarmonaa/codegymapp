# CodeGymApp Mobile — Plan de trabajo

## 1. Objetivo

Cerrar una versión Android estable y segura de CodeGymApp, alineada con:

- `docs/mobile_technical_spec.md`.
- `docs/mobile_final_qa.md`.
- La decisión de producto de mantener una aplicación personal de un solo usuario.

La biometría queda fuera del alcance. El modo offline permite lectura y escritura diferida mediante una cola local cifrada.

## 2. Reglas de ejecución

- Trabajar una fase a la vez y no mezclar correcciones de seguridad con cambios visuales no relacionados.
- Crear pruebas para las reglas críticas antes o junto con cada corrección.
- No borrar Room ni la cola offline durante cierres de sesión o migraciones.
- No cambiar contratos de API sin validar Android y PHP conjuntamente.
- Ejecutar compilación debug durante desarrollo y release al cerrar cada fase relevante.
- Documentar cualquier cambio de comportamiento en la especificación y el QA.
- No considerar terminada una tarea solamente porque compila; debe cumplir su criterio de aceptación.

## 3. Orden de prioridad

| Prioridad | Área | Resultado esperado |
| --- | --- | --- |
| P0 | Release | APK release generado sin errores de Lint o R8 |
| P0 | Sesión | Expiración segura con mensaje y Room preservado |
| P0 | Offline | Remapeo correcto de IDs locales a IDs del servidor |
| P0 | Offline | Cola sin pérdida silenciosa ni duplicados |
| P1 | Seguridad backend | Cloudflare, rate limiting, cron y HTTPS reforzados |
| P1 | Seguridad Android | Firebase, Keystore, JWT y privacidad push reforzados |
| P1 | Pruebas | Cobertura automatizada de sesión y sincronización |
| P2 | Robustez | Migraciones Room y diagnósticos operativos |
| P2 | UI/UX | Accesibilidad, skeletons y microinteracciones verificadas |
| P3 | Automatización | CI de compilación, pruebas y controles de seguridad |

### Estado al 13 de julio de 2026

- Completados: release, expiración de sesión, remapeo de IDs, consolidación básica de cola, endurecimiento cron/HTTPS, privacidad push, exclusión de `google-services.json`, esquema Room exportado y eliminación de migración destructiva.
- Completado: recuperación controlada ante clave Keystore no disponible; nunca se regenera la clave si la base cifrada ya existe.
- Completado: migración Room 5→6 y diagnóstico persistente con clasificación y backoff de sincronización.
- Completado: CI en `main` y `hosting`; la primera ejecución validó PHP 8.3, pruebas Android y release correctamente.
- Pendientes prioritarios: pruebas de integración cifradas y ejecución completa del QA manual en el teléfono.

## 4. Fase 0 — Preparación y línea base

### Tareas

- Confirmar que el árbol de trabajo no contiene cambios ajenos que puedan sobrescribirse.
- Registrar el resultado actual de:
  - `:app:compileDebugKotlin`.
  - `:app:assembleDebug`.
  - `:app:compileReleaseKotlin`.
  - `:app:assembleRelease`.
- Instalar o habilitar PHP CLI en el entorno de desarrollo para ejecutar lint del backend.
- Crear estructura `src/test` y `src/androidTest` si todavía no existe.
- Mantener un registro de pruebas manuales realizadas por dispositivo y versión de Android.

### Criterio de aceptación

- Existe una línea base reproducible con fallos conocidos y comandos documentados.
- Ningún cambio funcional se realiza antes de identificar el estado inicial.

## 5. Fase 1 — Desbloquear el release

Prioridad: P0.

### Tareas

1. Resolver el error Lint `InvalidFragmentVersionForActivityResult` de `MainActivity`.
2. Revisar el árbol de dependencias y declarar una versión AndroidX compatible sin introducir dependencias innecesarias.
3. Ejecutar Lint release.
4. Ejecutar R8/ProGuard y revisar advertencias.
5. Generar el APK release.
6. Inspeccionar el APK para comprobar:
   - Ausencia de archivos de configuración privados.
   - Ofuscación efectiva.
   - Presencia correcta de SQLCipher.
   - Compatibilidad de librerías nativas con páginas de 16 KB.

### Criterio de aceptación

- `:app:compileReleaseKotlin` termina correctamente.
- `:app:assembleRelease` termina correctamente.
- Retrofit, Moshi y Room funcionan en el build ofuscado.
- Room cifrado abre sin crash en un dispositivo o emulador release.

## 6. Fase 2 — Sesión y expiración segura

Prioridad: P0.

### Tareas

1. Conservar el motivo de cierre emitido por `SessionManager`.
2. Al recibir HTTP 401 o alcanzar cinco minutos de inactividad:
   - Persistir el borrado del JWT antes de finalizar el flujo.
   - Preservar Room.
   - Preservar la cola offline.
   - Limpiar completamente el backstack.
   - Navegar a Login.
   - Mostrar `Sesión expirada`.
3. Mantener el cierre manual sin mensaje de expiración.
4. Evitar múltiples eventos de expiración simultáneos por respuestas 401 concurrentes.
5. Evaluar invalidación de tokens después de cambio de contraseña mediante `token_version`; si se pospone, documentar que el riesgo máximo es la vigencia restante de 30 minutos.

### Pruebas

- Token presente antes de cinco minutos.
- Token eliminado después del tiempo límite.
- HTTP 401 elimina el token una sola vez.
- El backstack no permite volver a una pantalla autenticada.
- Room y acciones pendientes permanecen intactos.
- Login muestra el mensaje correcto.
- Logout manual no muestra “Sesión expirada”.

### Criterio de aceptación

- Todos los escenarios anteriores pasan en pruebas automatizadas y manuales.

## 7. Fase 3 — Integridad de la sincronización offline

Prioridad: P0.

### 7.1 Remapeo de IDs

1. Al crear un reto offline, conservar su ID local estable.
2. Cuando el servidor devuelva el ID definitivo:
   - Actualizar todas las copias del reto en caché.
   - Actualizar acciones pendientes que referencien el ID local.
   - Eliminar o reemplazar correctamente el registro temporal.
3. Ejecutar las acciones dependientes únicamente con el ID del servidor.

Casos obligatorios:

- Crear y editar offline.
- Crear y completar offline.
- Crear y marcar no cumplido offline.
- Crear y cancelar offline.
- Crear y reprogramar offline.

### 7.2 Consolidación de acciones

Definir reglas explícitas:

- Varias ediciones: conservar el estado final.
- Varias reprogramaciones: conservar la última fecha.
- Marcar notificación como leída repetidamente: una acción.
- Eliminar notificación después de marcarla: conservar solo lo necesario.
- Crear y cancelar antes de sincronizar: evitar crear en servidor cuando sea seguro hacerlo.
- Estados terminales contradictorios: rechazar o resolver antes de sincronizar.

### 7.3 Manejo de errores

- Una acción desconocida no se considera exitosa.
- Una acción fallida no se elimina.
- Diferenciar errores:
  - Red o servidor temporal: reintentar después.
  - HTTP 401: cerrar sesión y detener sincronización.
  - Validación permanente: conservar y mostrar diagnóstico.
- Evitar que una acción permanentemente inválida bloquee indefinidamente toda la cola, sin alterar el orden de acciones dependientes.
- Mostrar un único aviso amigable si quedan cambios sin sincronizar.

### 7.4 Refresco posterior

Después de sincronizar, refrescar:

- Mi día.
- Planeado.
- Retos.
- Metas.
- Resumen.
- Notificaciones.
- Detalles abiertos afectados.

### Criterio de aceptación

- Ninguna acción offline se pierde silenciosamente.
- No se envían IDs locales al backend.
- No aparecen duplicados en el servidor.
- La caché refleja el estado confirmado por la API.
- Los escenarios definidos pasan después de recuperar conexión.

## 8. Fase 4 — Seguridad del backend y Cloudflare

Prioridad: P1.

### 8.1 Rate limiting

- Obtener la IP desde `CF-Connecting-IP` solo cuando la conexión provenga de un proxy de Cloudflare confiable.
- Usar `REMOTE_ADDR` fuera de Cloudflare.
- Configurar límites separados para:
  - Login por IP.
  - Login por nombre de usuario.
  - API autenticada.
  - Push de prueba.
  - Endpoints cron.
- Evitar que el bloqueo de tres intentos pueda utilizarse fácilmente para denegar acceso al único usuario.
- Mantener mensajes externos uniformes para reducir enumeración de cuenta.

### 8.2 Cloudflare y User-Agent

- Mantener `MiAppEderSecure/2026` como identificador, no como credencial.
- No permitir que el User-Agent omita autenticación o controles esenciales.
- Aplicar reglas WAF por rutas y comportamiento.
- Mantener protección de bots y rate limiting para la API móvil.

### 8.3 Cron

- Cambiar recordatorios cron a `POST`.
- Aceptar el secreto exclusivamente en `X-Cron-Secret`.
- Retirar `?key=` de documentación y ejemplos.
- Rotar `CRON_SECRET` después de desplegar el cambio.
- Usar comparación constante con `hash_equals`.

### 8.4 HTTPS y cookies

- Configurar Cloudflare en Full (strict).
- Confirmar certificado válido en el origen.
- Redirigir HTTP a HTTPS.
- Reconocer proxy confiable sin aceptar encabezados falsificados.
- Marcar cookies `Secure` siempre en producción.
- Confirmar HSTS en Cloudflare y origen.

### 8.5 Web

- Añadir Content Security Policy gradualmente.
- Mover scripts inline a archivos o usar nonces.
- Mantener CSRF, `HttpOnly`, `SameSite` y encabezados actuales.
- Confirmar que `APP_DEBUG=false` en producción.

### Criterio de aceptación

- La IP utilizada por el rate limiter coincide con el cliente real sin confiar en encabezados arbitrarios.
- Ningún secreto aparece en URLs o logs normales.
- HTTP redirige a HTTPS y las cookies de producción siempre son seguras.
- El User-Agent no concede privilegios por sí solo.

## 9. Fase 5 — Seguridad Android

Prioridad: P1.

### 9.1 Firebase

- Retirar `android/app/google-services.json` del seguimiento Git.
- Suministrarlo mediante configuración local o CI.
- Restringir la API key por package y certificado de firma.
- Registrar SHA-256 del certificado release.
- Revisar clientes OAuth y APIs habilitadas.
- Considerar App Check donde sea compatible.
- Rotar credenciales únicamente si la revisión detecta exposición con impacto.

### 9.2 Keystore y almacenamiento cifrado

- Manejar clave Keystore invalidada o preferencias cifradas corruptas.
- No regenerar silenciosamente la clave si la base cifrada ya existe.
- Proporcionar diagnóstico y recuperación controlada.
- No borrar la cola sin confirmación explícita.
- Probar actualización, reinstalación y pérdida simulada de clave.

### 9.3 Red y logs

- Mover `logging-interceptor` a `debugImplementation`.
- No registrar `Authorization`, contraseña, cookies ni token FCM.
- Mantener HTTPS obligatorio.
- Evaluar pinning solo si existe un plan operativo de rotación; no implementarlo sin dicho plan.

### 9.4 Push

- Usar `NotificationCompat.VISIBILITY_PRIVATE`.
- Configurar texto público genérico para pantalla bloqueada.
- No enviar información sensible en payloads.
- Tratar `type` y `screen` solo como navegación a destinos permitidos.

### 9.5 R8

- Eliminar reglas biométricas no utilizadas.
- Reducir reglas `keep` demasiado amplias.
- Usar adaptadores Moshi generados cuando sea viable.
- Verificar que DTO, Room y Retrofit sigan funcionando.

### Criterio de aceptación

- No hay credenciales privadas rastreadas en el estado actual del repositorio.
- Las notificaciones no revelan contenido completo en pantalla bloqueada.
- La aplicación maneja fallos de almacenamiento cifrado de manera controlada.
- Release no registra información sensible.

## 10. Fase 6 — Pruebas automatizadas

Prioridad: P1.

### Pruebas unitarias mínimas

- `SessionManager` y expiración.
- Clasificación de errores de repositorio.
- Consolidación de la cola.
- Remapeo de IDs.
- Orden y reintentos de sincronización.
- Persistencia del último filtro.
- Validación de DTO y estados.

### Pruebas de integración

- Room cifrado abre con la clave correcta.
- Una clave incorrecta no produce recreación silenciosa.
- Cola persistida sobrevive a reinicio de proceso.
- Retrofit agrega Authorization y User-Agent.
- `HttpsOnlyInterceptor` rechaza HTTP.
- HTTP 401 genera el evento correcto.

### Backend

- Lint de todos los archivos PHP.
- Login válido e inválido.
- Expiración y firma JWT.
- Rate limiting.
- Autorización cron por encabezado.
- CSRF web.
- Validación de entradas de endpoints móviles.

### Criterio de aceptación

- Las reglas críticas tienen pruebas de regresión.
- Las pruebas pueden ejecutarse con un solo comando documentado por plataforma.

## 11. Fase 7 — Robustez de datos

Prioridad: P2.

### Tareas

- Sustituir `fallbackToDestructiveMigration()` por migraciones Room explícitas.
- Exportar esquemas Room al repositorio para validar migraciones.
- Probar migración desde la versión instalada anterior hasta la actual.
- Preservar cola y caché durante migraciones.
- Añadir fecha, intentos, último error y clasificación de error a diagnósticos de sincronización.
- Definir una política de reintentos y backoff.

### Criterio de aceptación

- Una actualización de la app no destruye datos locales ni acciones pendientes.
- Las migraciones están cubiertas por pruebas.

## 12. Fase 8 — UI/UX y accesibilidad

Prioridad: P2.

### Tareas

- Verificar skeletons en todas las pantallas definidas.
- Comprobar tamaños táctiles mínimos y navegación por lector de pantalla.
- Revisar contraste en temas claro y oscuro.
- Probar escalado de fuente.
- Confirmar que mensajes offline no se repiten.
- Confirmar háptica ligera/fuerte según especificación.
- Revisar animaciones y swipes sin pérdida de estado.

### Criterio de aceptación

- La app es usable con fuente grande y TalkBack básico.
- Los estados de carga, error, offline y sincronización son claros y no invasivos.

## 13. Fase 9 — CI y cierre de versión

Prioridad: P3.

### Automatización

- Compilar debug y release.
- Ejecutar pruebas unitarias.
- Ejecutar Lint Android.
- Ejecutar lint PHP.
- Ejecutar análisis de secretos y dependencias.
- Conservar artefactos de reportes, sin publicar secretos ni APK firmados de forma insegura.

### QA manual obligatorio

- Instalación limpia.
- Login, logout, HTTP 401 e inactividad.
- Ciclo online → offline → online.
- Todas las acciones offline de `mobile_final_qa.md`.
- Ausencia de duplicados.
- Push FCM/Azure y candado diario.
- Configuración de push y hora.
- Room cifrado en release.
- Compatibilidad 16 KB.
- R8/ProGuard.

### Criterio de aceptación

- Todos los puntos del QA están registrados como aprobados, fallidos o no aplicables con evidencia.
- No quedan defectos P0 o P1 abiertos.
- Se genera un APK/AAB release reproducible y probado.

## 14. Backlog fuera de alcance actual

- Autenticación biométrica.
- Soporte multiusuario y aislamiento por `user_id`.
- Refresh token.
- Reportes avanzados en móvil.
- Certificate pinning sin estrategia de rotación.

Estas funciones requieren una nueva decisión de producto y no deben incorporarse accidentalmente durante las fases anteriores.

## 15. Entregables por fase

Cada fase debe producir:

1. Código revisable y acotado.
2. Pruebas correspondientes.
3. Resultado de compilación o verificación.
4. Actualización documental si cambió el comportamiento.
5. Lista breve de riesgos residuales.

## 16. Definición de terminado

CodeGymApp Mobile se considera lista para cerrar versión cuando:

- Compila y genera release sin errores.
- Expira sesiones de forma segura y visible.
- Sincroniza todas las operaciones offline sin pérdida ni duplicación.
- Protege JWT, Room, claves Firebase y secretos backend.
- Funciona correctamente detrás de Cloudflare.
- Supera pruebas automatizadas y QA manual.
- No contiene pendientes P0 o P1.
