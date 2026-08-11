CREATE TABLE soft_machine_version (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(255) NOT NULL,
    xml_format VARCHAR(32) NOT NULL DEFAULT '1.0',
    xml_schema VARCHAR(255) NOT NULL DEFAULT 'x-schema:recipe_schema.xml',
    default_chamber_type VARCHAR(128) NULL,
    default_template VARCHAR(128) NOT NULL DEFAULT 'Default',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revise_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_smv_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE machine
    ADD COLUMN soft_machine_version_id BIGINT UNSIGNED NULL AFTER platform_type,
    ADD CONSTRAINT fk_machine_soft_machine_version
        FOREIGN KEY (soft_machine_version_id)
        REFERENCES soft_machine_version(id)
        ON DELETE SET NULL;

CREATE INDEX idx_machine_soft_machine_version_id ON machine (soft_machine_version_id);
