-- Limpia cancelaciones automaticas antiguas generadas por rutinas.
-- Ejecutar una vez despues de desplegar el cambio que deja de auto-cancelar retos.
-- Solo elimina retos de rutina cancelados sin datos capturados ni soluciones asociadas.

SET NAMES utf8mb4;

SELECT COUNT(*) AS auto_cancelled_routine_challenges_to_delete
FROM challenges c
WHERE c.status = 'cancelled'
  AND c.origin = 'routine'
  AND c.routine_id IS NOT NULL
  AND c.is_rescheduled = 0
  AND c.title IS NULL
  AND c.challenge_url IS NULL
  AND c.completed_date IS NULL
  AND c.time_spent_minutes IS NULL
  AND c.notes IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM challenge_languages cl
      WHERE cl.challenge_id = c.id
  )
  AND NOT EXISTS (
      SELECT 1
      FROM challenge_github_links gl
      WHERE gl.challenge_id = c.id
  );

DELETE c
FROM challenges c
WHERE c.status = 'cancelled'
  AND c.origin = 'routine'
  AND c.routine_id IS NOT NULL
  AND c.is_rescheduled = 0
  AND c.title IS NULL
  AND c.challenge_url IS NULL
  AND c.completed_date IS NULL
  AND c.time_spent_minutes IS NULL
  AND c.notes IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM challenge_languages cl
      WHERE cl.challenge_id = c.id
  )
  AND NOT EXISTS (
      SELECT 1
      FROM challenge_github_links gl
      WHERE gl.challenge_id = c.id
  );
