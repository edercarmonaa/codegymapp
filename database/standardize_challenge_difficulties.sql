-- Estandariza los niveles de retos a catalogo fijo.
-- Ejecutar una vez antes o durante el despliegue del cambio de aplicacion.
-- Valores finales permitidos: Facil, Medio, Dificil.

SET NAMES utf8mb4;

UPDATE challenges
SET difficulty = CASE
    WHEN LOWER(TRIM(difficulty)) IN ('facil', 'fácil', 'easy') THEN 'Facil'
    WHEN LOWER(TRIM(difficulty)) IN ('medio', 'media', 'medium', 'intermedio', 'intermedia') THEN 'Medio'
    WHEN LOWER(TRIM(difficulty)) IN ('dificil', 'difícil', 'hard') THEN 'Dificil'
    WHEN difficulty IS NULL OR TRIM(difficulty) = '' THEN NULL
    ELSE NULL
END;

ALTER TABLE challenges
    MODIFY difficulty ENUM('Facil', 'Medio', 'Dificil') NULL;
