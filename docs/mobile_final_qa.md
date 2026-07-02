# QA final móvil CodeGymApp

Usar esta lista antes de cerrar versión. Cada punto debe validarse online, offline cuando aplique, y después de recuperar conexión.

## Instalación y sesión

- Instalar APK limpio.
- Iniciar sesión con usuario y contraseña.
- Cerrar sesión y confirmar regreso a login.
- Validar expiración por inactividad de 5 minutos.
- Confirmar mensaje visible de sesión expirada.

## Offline y sincronización

- Abrir app con internet y confirmar que cachea catálogos.
- Quitar internet y confirmar banner superior “Sin conexión a internet”.
- Confirmar que no aparecen mensajes repetidos de “pendiente por sincronizar”.
- Crear reto offline.
- Editar reto offline.
- Completar reto offline.
- Marcar reto como no cumplido offline.
- Cancelar reto offline.
- Reprogramar reto offline.
- Crear rutina offline.
- Crear meta offline.
- Editar meta offline.
- Marcar notificación como leída offline.
- Eliminar notificación offline.
- Recuperar internet y confirmar que la cola sincroniza.
- Confirmar que Mi día, Planeado y Retos se refrescan después de sincronizar.
- Confirmar que no se duplican acciones repetidas.

## Pantallas principales

- Home.
- Mi día: pendientes y vencidos como acordeón.
- Planeado: grupos por fecha como acordeón.
- Retos: último filtro usado, incluye cancelados y todos.
- Detalle de reto: lectura offline y edición offline.
- Metas: crear/editar online y offline.
- Resumen: cambio de mes y lectura offline.
- Notificaciones: no leídas/historial, marcar con círculo, eliminar con swipe.
- Configuración: tema, push, hora recordatorio, sincronizar API y cerrar sesión.

## Push

- Registrar token FCM al iniciar sesión.
- En configuración activar/desactivar push.
- Cambiar hora de recordatorio.
- Presionar “Sincronizar API”.
- Confirmar en backend `mobile_device_tokens.push_enabled`.
- Confirmar en backend `mobile_device_tokens.reminder_time`.
- Enviar push de prueba.
- Enviar recordatorio de retos del día.
- Enviar recordatorio de retos vencidos.
- Confirmar candado de un envío diario salvo reenvío manual forzado.

## Microinteracciones y háptica

- Guardar produce feedback ligero.
- Completar produce feedback ligero.
- Cancelar produce feedback fuerte.
- Error crítico produce feedback fuerte.
- Acción guardada offline produce feedback ligero.
- Sincronización completada produce feedback ligero.
- Swipe exitoso conserva feedback existente.

## Release

- `:app:compileReleaseKotlin`.
- `:app:assembleRelease`.
- Validar compatibilidad 16 KB para librerías nativas.
- Validar ProGuard/R8 en release.
- Validar que SQLCipher abre Room cifrado sin crash.

