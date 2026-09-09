ALTER TABLE step_parameter
    ADD COLUMN computation_status VARCHAR(255) NULL AFTER user_modified,
    ADD COLUMN computed_at DATETIME(6) NULL AFTER computation_status;

CREATE INDEX idx_sp_computation_status ON step_parameter (computation_status);

CREATE TABLE computation_formula (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    target_step_code VARCHAR(128) NOT NULL,
    target_definition_path VARCHAR(255) NOT NULL,
    expression TEXT NOT NULL,
    rounding_mode VARCHAR(255) NOT NULL,
    decimals INT NULL,
    label VARCHAR(255) NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_cf_target UNIQUE (recipe_id, target_step_code, target_definition_path),
    CONSTRAINT fk_cf_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
) ENGINE=InnoDB;

CREATE INDEX idx_cf_recipe_id ON computation_formula (recipe_id);

CREATE TABLE formula_reference (
    id BIGINT NOT NULL AUTO_INCREMENT,
    formula_id BIGINT NOT NULL,
    slot INT NOT NULL,
    step_code VARCHAR(128) NOT NULL,
    definition_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_fr_slot UNIQUE (formula_id, slot),
    CONSTRAINT fk_fr_formula FOREIGN KEY (formula_id) REFERENCES computation_formula (id)
) ENGINE=InnoDB;

CREATE INDEX idx_fr_source ON formula_reference (step_code, definition_path);
CREATE INDEX idx_fr_formula_id ON formula_reference (formula_id);
