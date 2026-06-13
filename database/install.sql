-- CodeGymApp - Instalador MySQL
-- Requisitos: MySQL 8+ o MariaDB reciente con InnoDB y utf8mb4.
-- No crea usuarios de aplicación. Ejecuta tools/create_user.php después de importar.

SET NAMES utf8mb4;
SET time_zone = '-06:00';

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    singleton_key TINYINT UNSIGNED NOT NULL DEFAULT 1,
    name VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    preferred_theme ENUM('light', 'dark') NOT NULL DEFAULT 'light',
    failed_login_attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY users_singleton_key_unique (singleton_key),
    UNIQUE KEY users_username_unique (username),
    UNIQUE KEY users_email_unique (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platforms (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NULL,
    url VARCHAR(255) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY platforms_name_unique (name),
    KEY platforms_is_active_index (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE languages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY languages_name_unique (name),
    KEY languages_is_active_index (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE routines (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    platform_id BIGINT UNSIGNED NOT NULL,
    frequency_type ENUM('daily', 'weekly', 'monthly') NOT NULL,
    week_days VARCHAR(30) NULL,
    month_day TINYINT UNSIGNED NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY routines_platform_id_index (platform_id),
    KEY routines_active_dates_index (is_active, start_date, end_date),
    CONSTRAINT routines_platform_id_foreign
        FOREIGN KEY (platform_id) REFERENCES platforms (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE challenges (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    platform_id BIGINT UNSIGNED NOT NULL,
    routine_id BIGINT UNSIGNED NULL,
    title VARCHAR(180) NULL,
    challenge_url VARCHAR(255) NULL,
    difficulty VARCHAR(120) NULL,
    scheduled_date DATE NOT NULL,
    original_scheduled_date DATE NULL,
    completed_date DATE NULL,
    status ENUM('pending', 'completed', 'expired', 'missed', 'cancelled') NOT NULL DEFAULT 'pending',
    origin ENUM('calendar', 'routine', 'manual') NOT NULL DEFAULT 'calendar',
    time_spent_minutes INT UNSIGNED NULL,
    notes TEXT NULL,
    is_rescheduled TINYINT(1) NOT NULL DEFAULT 0,
    reschedule_count INT UNSIGNED NOT NULL DEFAULT 0,
    last_rescheduled_date DATE NULL,
    is_locked TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY challenges_platform_id_index (platform_id),
    KEY challenges_routine_id_index (routine_id),
    KEY challenges_status_date_index (status, scheduled_date),
    KEY challenges_completed_date_index (completed_date),
    KEY challenges_origin_index (origin),
    CONSTRAINT challenges_platform_id_foreign
        FOREIGN KEY (platform_id) REFERENCES platforms (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT challenges_routine_id_foreign
        FOREIGN KEY (routine_id) REFERENCES routines (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE challenge_languages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT UNSIGNED NOT NULL,
    language_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY challenge_languages_unique (challenge_id, language_id),
    KEY challenge_languages_language_id_index (language_id),
    CONSTRAINT challenge_languages_challenge_id_foreign
        FOREIGN KEY (challenge_id) REFERENCES challenges (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT challenge_languages_language_id_foreign
        FOREIGN KEY (language_id) REFERENCES languages (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE challenge_github_links (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT UNSIGNED NOT NULL,
    language_id BIGINT UNSIGNED NULL,
    github_url VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY challenge_github_links_challenge_id_index (challenge_id),
    KEY challenge_github_links_language_id_index (language_id),
    CONSTRAINT challenge_github_links_challenge_id_foreign
        FOREIGN KEY (challenge_id) REFERENCES challenges (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT challenge_github_links_language_id_foreign
        FOREIGN KEY (language_id) REFERENCES languages (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE goals (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    goal_type ENUM('completed_challenges', 'practice_time', 'streak') NOT NULL,
    period_type ENUM('weekly', 'monthly', 'annual') NOT NULL,
    target_value INT UNSIGNED NOT NULL,
    platform_id BIGINT UNSIGNED NULL,
    language_id BIGINT UNSIGNED NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    current_value INT UNSIGNED NOT NULL DEFAULT 0,
    progress_percent DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    status ENUM('active', 'closed') NOT NULL DEFAULT 'active',
    auto_renew TINYINT(1) NOT NULL DEFAULT 0,
    source_goal_id BIGINT UNSIGNED NULL,
    closed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY goals_status_period_index (status, period_start, period_end),
    KEY goals_platform_id_index (platform_id),
    KEY goals_language_id_index (language_id),
    KEY goals_source_goal_id_index (source_goal_id),
    CONSTRAINT goals_platform_id_foreign
        FOREIGN KEY (platform_id) REFERENCES platforms (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT goals_language_id_foreign
        FOREIGN KEY (language_id) REFERENCES languages (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT goals_source_goal_id_foreign
        FOREIGN KEY (source_goal_id) REFERENCES goals (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    type VARCHAR(80) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME NULL,
    action_url VARCHAR(255) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY notifications_unread_index (is_active, is_read, created_at),
    KEY notifications_type_index (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE security_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL,
    event_type VARCHAR(80) NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    result ENUM('success', 'failure', 'warning') NOT NULL,
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY security_logs_user_id_index (user_id),
    KEY security_logs_event_type_index (event_type),
    KEY security_logs_created_at_index (created_at),
    CONSTRAINT security_logs_user_id_foreign
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platforms (name, description, url) VALUES
('LeetCode', 'Plataforma de retos de programación y entrevistas.', 'https://leetcode.com'),
('HackerRank', 'Retos de programación, SQL y entrevistas.', 'https://www.hackerrank.com'),
('Codewars', 'Katas de programación por niveles.', 'https://www.codewars.com'),
('Exercism', 'Práctica guiada por lenguajes.', 'https://exercism.org'),
('Beecrowd', 'Problemas de programación competitiva.', 'https://www.beecrowd.com.br'),
('Codeforces', 'Programación competitiva.', 'https://codeforces.com'),
('Project Euler', 'Retos matemáticos y de programación.', 'https://projecteuler.net');

INSERT INTO languages (name) VALUES
('PHP'),
('JavaScript'),
('Python'),
('C'),
('Objective-C'),
('SQL'),
('Java'),
('C++');

