# CodeGymApp Mobile — Especificación técnica vigente

## 1. Estado del documento

Esta es la especificación técnica vigente de CodeGymApp Mobile. Sustituye las reglas contradictorias de la especificación inicial usada al comenzar el proyecto.

La lista operativa de aceptación y pruebas se mantiene en `docs/mobile_final_qa.md`. Si ambos documentos difieren, esta especificación define el comportamiento esperado y la lista QA define cómo comprobarlo.

## 2. Objetivo

CodeGymApp Mobile es una aplicación Android nativa para uso personal que permite:

- Consultar retos de hoy, planeados y del mes.
- Revisar retos vencidos.
- Crear, editar, completar, cancelar, reprogramar y marcar retos como no cumplidos.
- Registrar retos ya realizados.
- Crear rutinas y metas.
- Consultar y editar metas activas.
- Consultar un resumen mensual básico.
- Administrar notificaciones y preferencias.
- Trabajar offline mediante datos cifrados y una cola local de cambios.
- Sincronizar automáticamente al recuperar conexión.

La app debe ser rápida, minimalista y adecuada para uso diario.

## 3. Tecnología obligatoria

- Kotlin.
- Jetpack Compose y Material Design 3.
- Navigation Compose.
- MVVM, Repository Pattern y StateFlow.
- Inyección manual de dependencias; no usar Hilt en esta versión.
- Room cifrado con SQLCipher.
- Retrofit y OkHttp.
- EncryptedSharedPreferences y Android Keystore.
- Firebase Cloud Messaging como canal Android.
- Azure Notification Hubs para entrega push desde el backend.

## 4. API y autenticación

La app consume la API PHP del proyecto CodeGymApp mediante HTTPS.

- Usar un JWT de acceso con duración de 30 minutos.
- La sesión es stateless.
- No usar refresh token.
- Enviar `Authorization: Bearer <JWT>` en peticiones autenticadas.
- Enviar `User-Agent: MiAppEderSecure/2026` en todas las peticiones.
- Rechazar tráfico HTTP mediante `HttpsOnlyInterceptor` y `usesCleartextTraffic="false"`.

Interceptores requeridos:

1. `AuthHeaderInterceptor`: agrega JWT y User-Agent.
2. `UnauthorizedInterceptor`: detecta HTTP 401 y cierra la sesión.
3. `HttpsOnlyInterceptor`: impide solicitudes que no usen HTTPS.

## 5. Sesión y almacenamiento seguro

### 5.1 JWT

- Guardar el JWT exclusivamente en EncryptedSharedPreferences protegido por Android Keystore.
- No guardar el JWT en Room, SharedPreferences normales ni archivos planos.

### 5.2 Cierre de sesión

Ante HTTP 401 o cinco minutos de inactividad:

1. Borrar el JWT.
2. Preservar Room y la cola local.
3. Limpiar completamente el backstack.
4. Navegar a Login.
5. Mostrar `Sesión expirada` cuando el cierre no sea manual.

El cierre manual también preserva Room, pero no muestra el mensaje de expiración.

### 5.3 Room

- Cifrar la base local con SQLCipher.
- Guardar la frase de cifrado protegida mediante Android Keystore.
- Cargar SQLCipher antes de abrir Room.
- No borrar Room al cerrar sesión.
- Configurar `android:allowBackup="false"`.

## 6. Autenticación biométrica

La autenticación biométrica no forma parte de esta versión.

- No almacenar contraseñas para reautenticación biométrica.
- No permitir acceso sin JWT mediante huella.
- Cuando el JWT expire, exigir un nuevo login online con usuario y contraseña.
- La ausencia de biometría no se considera un incumplimiento.

## 7. Login y navegación

El login usa usuario, contraseña y la API JWT. Después de autenticar:

- Guardar el JWT cifrado.
- Registrar o actualizar el token FCM.
- Iniciar sincronización.
- Entrar a la experiencia principal.

La navegación principal debe ofrecer acceso claro a:

- Home.
- Mi día.
- Planeado.
- Retos.
- Más o accesos secundarios: Metas, Resumen, Notificaciones, Cuenta y Configuración.

Las pantallas de trabajo rápido deben usar navegación inferior y acciones al alcance del pulgar. El botón `+` abre:

- Programar reto.
- Registrar reto realizado.
- Crear rutina.
- Crear meta.

## 8. Modo offline

El modo offline permite lectura y escritura diferida.

Sin conexión se puede:

- Consultar datos previamente almacenados en Room.
- Crear retos.
- Editar retos, incluidos retos locales aún no sincronizados.
- Completar, cancelar, reprogramar o marcar retos como no cumplidos.
- Crear rutinas.
- Crear y editar metas.
- Marcar notificaciones como leídas.
- Eliminar notificaciones.

Cada cambio offline debe:

1. Actualizar inmediatamente la caché local cuando aplique.
2. Guardarse en una cola persistente cifrada.
3. Mostrar un mensaje amigable indicando que se sincronizará después.
4. Sincronizarse al recuperar conexión.

No mostrar repetidamente mensajes de “pendiente por sincronizar” ni un indicador permanente de cambios pendientes.

## 9. Cola y sincronización

La sincronización se ejecuta:

- Al abrir la app con sesión activa.
- Al recuperar conexión.
- Al volver a primer plano.
- Después del login.
- Al solicitar sincronización manual.

Reglas obligatorias:

- Procesar acciones en orden seguro.
- Evitar acciones duplicadas o consolidarlas cuando representen el mismo estado final.
- Si se crea una entidad offline, sustituir su ID local por el ID asignado por el servidor en todas las acciones dependientes.
- No eliminar acciones desconocidas o fallidas como si fueran exitosas.
- Un error permanente debe quedar diagnosticado sin provocar pérdida silenciosa de datos.
- Un error transitorio no debe descartar la acción.
- Después de sincronizar, refrescar Mi día, Planeado, Retos, metas y demás vistas afectadas.
- Si hay fallos, mostrar: `No se pudieron sincronizar algunos cambios. Se intentará más tarde.`

## 10. Pantallas

### 10.1 Home

Ofrece accesos rápidos, resumen de actividad y creación rápida sin saturar la interfaz.

### 10.2 Mi día

- Mostrar pendientes de hoy y vencidos por revisar.
- Usar secciones tipo acordeón.
- Pendientes abiertos por defecto.
- Vencidos cerrados por defecto con contador.
- Las tarjetas muestran principalmente la plataforma.
- Tocar una tarjeta abre el detalle en Bottom Sheet.

### 10.3 Planeado

- Mostrar retos pendientes futuros.
- Agruparlos por fecha en secciones tipo agenda/acordeón.
- No incluir cumplidos, vencidos, cancelados ni no cumplidos.

### 10.4 Retos

- Mostrar inicialmente el mes actual.
- El filtro inicial es `Pendientes`.
- Recordar localmente el último filtro.
- Permitir Pendientes, Cumplidos, Vencidos, No cumplidos, Cancelados y Todos.

### 10.5 Detalle de reto

El Bottom Sheet permite:

- Consultar y editar detalles.
- Completar.
- Reprogramar.
- Marcar como no cumplido.
- Cancelar con confirmación.

Para completar se requieren nombre, dificultad, tiempo invertido y al menos un lenguaje. GitHub es opcional.

Los gestos admitidos son:

- Swipe derecha: completar; si faltan datos, abrir el formulario.
- Swipe izquierda: acciones rápidas para reprogramar, no cumplido y cancelar.

### 10.6 Metas

- Mostrar metas activas.
- Permitir crear y editar online u offline.
- Representar progreso con tarjeta y barra.

### 10.7 Resumen

- Permitir seleccionar mes.
- Mostrar información básica, métricas y distribución de estados.
- Permitir lectura desde caché offline.

### 10.8 Notificaciones

- Separar no leídas e historial.
- Marcar como leída mediante indicador circular.
- Eliminar con swipe.
- Admitir ambas acciones offline.

### 10.9 Configuración

Incluir:

- Tema Sistema, Claro u Oscuro.
- Activar o desactivar push.
- Hora del recordatorio.
- Sincronizar con la API.
- Cerrar sesión.

El tema se guarda localmente y se sincroniza con web/API. La hora predeterminada vigente es `08:00`.

## 11. Push notifications

- Registrar el token FCM después del login.
- Sincronizar `push_enabled` y `reminder_time` con el backend.
- Usar Azure Notification Hubs para la entrega.
- Permitir push de prueba.
- Enviar recordatorios de retos pendientes de hoy y vencidos por revisar.
- Mantener candado de un envío diario, salvo reenvío manual forzado.
- No enviar push por operaciones rutinarias como guardar un reto.

Otros avisos importantes, como metas en riesgo o completadas, pueden incorporarse cuando exista soporte de backend y QA específico.

## 12. UI, carga y accesibilidad

- Usar Material Design 3, tipografía del sistema y unidades escalables.
- Mantener alto contraste y evitar negro puro en modo oscuro.
- Usar skeletons como carga principal en Home/Cuenta, Mi día, Planeado, Retos, Metas, Resumen y Notificaciones.
- Mantener whitespace, elevación suave y bajo ruido visual.
- Usar Bottom Sheets para formularios y acciones contextuales.
- Conservar acciones frecuentes en la zona inferior y al alcance del pulgar.

## 13. Microinteracciones y háptica

- Feedback ligero al guardar, completar, guardar offline o terminar una sincronización.
- Feedback fuerte al cancelar o ante un error crítico.
- Mantener feedback en swipes exitosos.
- Mostrar estados visuales claros durante guardado, error y cambio de estado.

## 14. Seguridad de release

- Activar R8/ProGuard y reducción de recursos en release.
- Conservar las reglas necesarias para Retrofit, OkHttp, Moshi, Room y SQLCipher.
- Verificar que la ofuscación no rompa DTO, entidades ni serialización.
- Compilar `:app:compileReleaseKotlin` y `:app:assembleRelease` sin errores.
- Validar apertura de Room cifrado en un build release.
- Validar compatibilidad de librerías nativas con páginas de memoria de 16 KB.
- No incluir secretos, contraseñas ni credenciales privadas en el APK o repositorio.

## 15. Criterios de aceptación

La versión solo puede cerrarse cuando:

- Cumple la lista de `docs/mobile_final_qa.md`.
- No pierde acciones offline.
- No duplica acciones al sincronizar.
- Maneja correctamente IDs locales y del servidor.
- Muestra el cierre de sesión expirado.
- Genera un release con R8 sin errores.
- Se validan manualmente login, offline, reconexión, push y Room cifrado en dispositivo.

