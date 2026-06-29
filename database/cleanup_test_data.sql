-- CodeGymApp - Limpieza de datos de prueba
-- Conserva usuarios y catalogos base: users, platforms, languages.
-- Recomendado: exporta un respaldo completo antes de ejecutar este script.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM notifications;
DELETE FROM security_logs;

DELETE FROM challenge_github_links;
DELETE FROM challenge_languages;
DELETE FROM challenges;
DELETE FROM routines;

DELETE FROM goals;

ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE security_logs AUTO_INCREMENT = 1;
ALTER TABLE challenge_github_links AUTO_INCREMENT = 1;
ALTER TABLE challenge_languages AUTO_INCREMENT = 1;
ALTER TABLE challenges AUTO_INCREMENT = 1;
ALTER TABLE routines AUTO_INCREMENT = 1;
ALTER TABLE goals AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;
