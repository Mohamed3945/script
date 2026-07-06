CREATE TABLE machine (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    platform_type ENUM('CENTURA', 'VANTAGE') NOT NULL DEFAULT 'CENTURA',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_machine_code UNIQUE (code),
    CONSTRAINT uq_machine_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chamber (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_chamber_machine
        FOREIGN KEY (machine_id)
        REFERENCES machine(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_chamber_machine_code UNIQUE (machine_id, code),
    CONSTRAINT uq_chamber_machine_name UNIQUE (machine_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chamber_capability (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    label VARCHAR(255) NOT NULL,
    category ENUM('TECHNO', 'MODE', 'TEMPERATURE', 'PRESSURE', 'GAS_FLOW', 'OTHER') NOT NULL DEFAULT 'OTHER',
    active TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_chamber_capability_code UNIQUE (code),
    CONSTRAINT uq_chamber_capability_label UNIQUE (label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chamber_capability_link (
    chamber_id BIGINT UNSIGNED NOT NULL,
    capability_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chamber_id, capability_id),
    CONSTRAINT fk_ccl_chamber
        FOREIGN KEY (chamber_id)
        REFERENCES chamber(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ccl_capability
        FOREIGN KEY (capability_id)
        REFERENCES chamber_capability(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE configuration_definition (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value_type ENUM('BOOLEAN', 'NUMBER', 'ENUM', 'TEXT') NOT NULL DEFAULT 'NUMBER',
    unit VARCHAR(64) NULL,
    question_for_form VARCHAR(500) NOT NULL,
    question_group VARCHAR(255) NULL,
    display_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_configuration_definition_code UNIQUE (code),
    CONSTRAINT uq_configuration_definition_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chamber_configuration (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    chamber_id BIGINT UNSIGNED NOT NULL,
    configuration_definition_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    nominal_value DECIMAL(12,3) NULL,
    min_value DECIMAL(12,3) NULL,
    max_value DECIMAL(12,3) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_chamber_configuration UNIQUE (chamber_id, configuration_definition_id, code),
    CONSTRAINT fk_chamber_configuration_chamber
        FOREIGN KEY (chamber_id)
        REFERENCES chamber(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chamber_configuration_definition
        FOREIGN KEY (configuration_definition_id)
        REFERENCES configuration_definition(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
