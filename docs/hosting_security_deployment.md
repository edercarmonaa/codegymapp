# CodeGymApp — Despliegue de seguridad en hosting

## Objetivo

Aplicar el cambio de autenticación de tareas cron sin dejar secretos en URLs ni access logs.

## Estado confirmado

- Cloudflare usa SSL/TLS `Full (strict)`.
- El access log de Apache muestra IPs reales de clientes, por lo que el hosting ya restaura correctamente la IP a `REMOTE_ADDR`.
- El secreto cron anterior apareció en query strings y debe considerarse expuesto.

## Orden de despliegue

1. Pausar temporalmente las dos tareas cron actuales de cPanel.
2. Generar un secreto nuevo, distinto de `JWT_SECRET`:

   ```bash
   openssl rand -hex 32
   ```

3. Reemplazar `CRON_SECRET` en el `.env` del hosting.
4. Desplegar la rama `hosting` actualizada.
5. Sustituir las tareas cron por comandos `POST` con encabezado:

   ```bash
   curl -fsS -X POST -H "X-Cron-Secret: NUEVO_SECRETO" "https://codegym.karedit.com.mx/api/cron/mobile/today-reminder"
   curl -fsS -X POST -H "X-Cron-Secret: NUEVO_SECRETO" "https://codegym.karedit.com.mx/api/cron/mobile/expired-review-reminder"
   ```

6. Programar ambas tareas con `*/5 * * * *`. El backend comprueba la hora configurada por el usuario y mantiene un candado diario.
7. Ejecutar manualmente una vez y confirmar HTTP 200.
8. Confirmar que el access log contiene la ruta, pero no el secreto.
9. Confirmar que una petición sin `X-Cron-Secret` recibe HTTP 403.

## Reenvío manual forzado

Usar únicamente durante QA:

```bash
curl -fsS -X POST \
  -H "X-Cron-Secret: NUEVO_SECRETO" \
  -H "X-Cron-Force: 1" \
  "https://codegym.karedit.com.mx/api/cron/mobile/today-reminder"
```

No guardar el secreto nuevo en Git, documentación, capturas o tickets.

## Rollback

Si el despliegue falla:

1. Mantener las tareas cron pausadas.
2. Revertir el código desde cPanel.
3. No restaurar el secreto expuesto.
4. Corregir el despliegue y conservar el nuevo secreto para el siguiente intento.
