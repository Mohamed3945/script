CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT uq_app_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE app_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_app_role_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE app_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_app_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_role_role FOREIGN KEY (role_id) REFERENCES app_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO app_role (code, label, create_time, revise_time)
VALUES
    ('SIMPLE', 'Utilisateur', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('SUPER', 'Super utilisateur', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT IGNORE INTO app_user (id, username, password_hash, display_name, email, active, create_time, revise_time)
SELECT legacy_user_id,
       CONCAT('legacy_user_', legacy_user_id),
       '{bcrypt}legacy-disabled',
       CONCAT('Legacy user ', legacy_user_id),
       NULL,
       b'0',
       CURRENT_TIMESTAMP(6),
       CURRENT_TIMESTAMP(6)
FROM (
    SELECT creator AS legacy_user_id FROM recipe WHERE creator IS NOT NULL
    UNION
    SELECT revisor AS legacy_user_id FROM recipe WHERE revisor IS NOT NULL
    UNION
    SELECT creator_id AS legacy_user_id FROM decision_execution WHERE creator_id IS NOT NULL
) legacy_users;

ALTER TABLE recipe
    ADD CONSTRAINT fk_recipe_creator_user FOREIGN KEY (creator) REFERENCES app_user(id),
    ADD CONSTRAINT fk_recipe_revisor_user FOREIGN KEY (revisor) REFERENCES app_user(id);

ALTER TABLE decision_execution
    ADD CONSTRAINT fk_decision_execution_creator_user FOREIGN KEY (creator_id) REFERENCES app_user(id);