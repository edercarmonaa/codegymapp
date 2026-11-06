# Android 1.1.0 (versionCode 2)

Actualización del 6 de noviembre de 2026. Implementada sobre `main` para consumir
el contrato de dificultades existente en la rama `hosting`.

## Catálogo de dificultades

- Android lee `difficulty_options` de `GET /api/mobile/create-options`, conserva
  claves y etiquetas del servidor y guarda las opciones en Room para uso offline.
- Registro manual y edición usan el mismo selector. El registro exige una opción
  válida; la edición permite dejarla vacía para guardar un reto incompleto.
- La sincronización de metas ya no sobrescribe el catálogo de dificultades.
- Si un backend anterior omite el campo, se conserva el catálogo almacenado o se
  usan los niveles originales `facil`, `medio`, `dificil`. Un catálogo recibido
  explícitamente vacío elimina las opciones anteriores.
- Valores históricos con acentos, mayúsculas y equivalencias conocidas
  (`easy`, `medium`, `hard`, `intermedio`) se normalizan al editar y sincronizar.
  Los valores desconocidos no se convierten silenciosamente en otro nivel.

## Reglas de retos

- No se permite cancelar un reto reprogramado. La interfaz explica la restricción
  y los repositorios la validan antes de enviar o encolar la acción.
- Reprogramar exige una fecha actual o futura. Offline también cambia el estado
  a `pending` y limpia la fecha de cumplimiento, igual que el backend de `hosting`.
- Cancelados y no cumplidos se consultan sin permitir guardar cambios.
- Las tarjetas identifican los retos reprogramados.

## Revisión de acciones pendientes

Configuración muestra las acciones rechazadas por validación con el mensaje del
servidor. Se puede corregir la dificultad o la fecha según el tipo de acción y
reintentar. La edición conserva los demás campos e identificadores; no elimina
la acción. Para otros rechazos, el usuario puede revisar el reto y reintentar.
El botón general de sincronización también solicita sincronizar la cola.

No hay cambios de esquema Room ni eliminación de datos locales. Esta actualización
no aplica migraciones SQL ni despliega cambios en el servidor. La normalización
de la base del servidor sigue correspondiendo al despliegue de `hosting`.

## Validación

Automatizada: deserialización de opciones con y sin catálogo, persistencia de
etiquetas y orden, conservación frente a inicialización estática, catálogo vacío,
normalización histórica, reglas de fechas y cancelación, y corrección de una
acción rechazada sin alterar los demás datos.

Resultado automatizado: 24 pruebas aprobadas, sin fallos ni errores. Compilaciones debug y release 1.1.0 completadas, con Lint vital y optimización R8 aprobados. El APK release se genera sin firma; para distribuir una actualización se requiere la clave habitual.

Comando de validación:

```text
gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

QA pendiente en dispositivo:

1. Instalar como actualización conservando base cifrada y cola offline.
2. Registrar y editar retos con las tres dificultades; comprobarlos en la web.
3. Cambiar una etiqueta en el backend y volver a abrir el formulario conectado;
   comprobarla de nuevo sin conexión.
4. Editar un reto histórico con dificultad acentuada o antigua.
5. Reprogramar un vencido offline; comprobar estado pendiente y sincronización.
6. Comprobar el bloqueo de cancelación de reprogramados y la consulta de cerrados.
7. Provocar un rechazo de dificultad o fecha, corregirlo en Configuración y
   verificar que se sincroniza una sola vez sin perder otros campos.
8. Verificar la misma conducta en un APK release firmado con la clave habitual.
