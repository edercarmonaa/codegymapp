-- Limpia retos duplicados generados por rutina y evita que vuelvan a crearse.
-- Ejecutar una sola vez en hosting despues de respaldar la base de datos.

START TRANSACTION;

CREATE TEMPORARY TABLE routine_challenge_keep AS
SELECT
    routine_id,
    scheduled_date,
    is_rescheduled,
    CAST(SUBSTRING_INDEX(
        GROUP_CONCAT(
            id
            ORDER BY
                CASE status
                    WHEN 'completed' THEN 1
                    WHEN 'missed' THEN 2
                    WHEN 'cancelled' THEN 3
                    WHEN 'expired' THEN 4
                    WHEN 'pending' THEN 5
                    ELSE 6
                END,
                id ASC
        ),
        ',',
        1
    ) AS UNSIGNED) AS keep_id
FROM challenges
WHERE routine_id IS NOT NULL
GROUP BY routine_id, scheduled_date, is_rescheduled
HAVING COUNT(*) > 1;

CREATE TEMPORARY TABLE routine_challenge_delete AS
SELECT c.id
FROM challenges c
JOIN routine_challenge_keep k
  ON k.routine_id = c.routine_id
 AND k.scheduled_date = c.scheduled_date
 AND k.is_rescheduled = c.is_rescheduled
WHERE c.id <> k.keep_id;

DELETE gl
FROM challenge_github_links gl
JOIN routine_challenge_delete d ON d.id = gl.challenge_id;

DELETE cl
FROM challenge_languages cl
JOIN routine_challenge_delete d ON d.id = cl.challenge_id;

DELETE c
FROM challenges c
JOIN routine_challenge_delete d ON d.id = c.id;

DROP TEMPORARY TABLE routine_challenge_delete;
DROP TEMPORARY TABLE routine_challenge_keep;

COMMIT;
