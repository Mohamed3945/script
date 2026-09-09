-- Baseline schema generated from V1..V33 without top-level DML statements.
-- Purpose: create schema only on brand-new databases before loading data-only dumps.

CREATE TABLE decision_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    code VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    is_entry_point BIT(1) NOT NULL,
    label VARCHAR(255) NOT NULL,
    order_index INT(11) NOT NULL,
    question_type ENUM('BOOLEAN','INFO','SINGLE_CHOICE') NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_decision_question_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE decision_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    create_time DATETIME(6) NOT NULL,
    label VARCHAR(255) NOT NULL,
    order_index INT(11) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    value VARCHAR(255) NOT NULL,
    question_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_decision_option_question_id (question_id),
    CONSTRAINT fk_decision_option_question
        FOREIGN KEY (question_id) REFERENCES decision_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE decision_result_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    code VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    description TEXT DEFAULT NULL,
    golden_recipe_id BIGINT DEFAULT NULL,
    machine_id BIGINT DEFAULT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_decision_result_profile_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE decision_transition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    current_question_id BIGINT NOT NULL,
    next_question_id BIGINT DEFAULT NULL,
    option_id BIGINT NOT NULL,
    result_profile_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_decision_transition_current_question_id (current_question_id),
    KEY idx_decision_transition_next_question_id (next_question_id),
    KEY idx_decision_transition_option_id (option_id),
    KEY idx_decision_transition_result_profile_id (result_profile_id),
    CONSTRAINT fk_decision_transition_next_question
        FOREIGN KEY (next_question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_decision_transition_result_profile
        FOREIGN KEY (result_profile_id) REFERENCES decision_result_profile(id),
    CONSTRAINT fk_decision_transition_current_question
        FOREIGN KEY (current_question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_decision_transition_option
        FOREIGN KEY (option_id) REFERENCES decision_option(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
ALTER TABLE decision_question AUTO_INCREMENT = 8;
ALTER TABLE decision_option AUTO_INCREMENT = 13;
ALTER TABLE decision_result_profile AUTO_INCREMENT = 8;
ALTER TABLE decision_transition AUTO_INCREMENT = 12;
CREATE TABLE IF NOT EXISTS recipe (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipe_kind VARCHAR(255) NOT NULL,
    parent_recipe_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    creator BIGINT NOT NULL,
    revisor BIGINT NULL,
    process_family VARCHAR(255) NULL,
    status VARCHAR(255) NOT NULL,
    version INT NOT NULL,
    frozen BIT(1) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_recipe_parent_recipe FOREIGN KEY (parent_recipe_id) REFERENCES recipe (id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS step (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    step_kind VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_step_recipe_order UNIQUE (recipe_id, order_index),
    CONSTRAINT fk_step_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS parameter_definition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    unit VARCHAR(255) NULL,
    description TEXT NULL,
    value_type VARCHAR(255) NOT NULL,
    required_on_step BIT(1) NOT NULL,
    default_value_json LONGTEXT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_parameter_definition_name UNIQUE (name),
    CONSTRAINT uq_parameter_definition_alias UNIQUE (alias),
    CONSTRAINT chk_parameter_definition_default_json CHECK (default_value_json IS NULL OR JSON_VALID(default_value_json))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS parameter_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    definition_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_po_definition_label UNIQUE (definition_id, label),
    CONSTRAINT fk_parameter_option_definition FOREIGN KEY (definition_id) REFERENCES parameter_definition (id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS step_parameter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    step_id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    parent_step_parameter_id BIGINT NULL,
    parent_order_scope BIGINT NOT NULL,
    order_index INT NOT NULL,
    label_override VARCHAR(255) NULL,
    value_json LONGTEXT NULL,
    selected_option_id BIGINT NULL,
    activation_state VARCHAR(255) NOT NULL,
    locked_by_golden BIT(1) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_sp_order_in_scope UNIQUE (step_id, parent_order_scope, order_index),
    CONSTRAINT uq_sp_definition_per_scope UNIQUE (step_id, parent_order_scope, definition_id),
    CONSTRAINT fk_step_parameter_step FOREIGN KEY (step_id) REFERENCES step (id),
    CONSTRAINT fk_step_parameter_definition FOREIGN KEY (definition_id) REFERENCES parameter_definition (id),
    CONSTRAINT fk_step_parameter_parent FOREIGN KEY (parent_step_parameter_id) REFERENCES step_parameter (id),
    CONSTRAINT fk_step_parameter_selected_option FOREIGN KEY (selected_option_id) REFERENCES parameter_option (id),
    CONSTRAINT chk_step_parameter_value_or_option CHECK (NOT (value_json IS NOT NULL AND selected_option_id IS NOT NULL)),
    CONSTRAINT chk_step_parameter_value_json CHECK (value_json IS NULL OR JSON_VALID(value_json))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS parameter_dependency_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_definition_id BIGINT NOT NULL,
    trigger_option_id BIGINT NOT NULL,
    target_definition_id BIGINT NOT NULL,
    effect VARCHAR(255) NOT NULL,
    scope VARCHAR(255) NOT NULL,
    priority INT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pdr_source_trigger_target UNIQUE (source_definition_id, trigger_option_id, target_definition_id),
    CONSTRAINT fk_pdr_source_definition FOREIGN KEY (source_definition_id) REFERENCES parameter_definition (id),
    CONSTRAINT fk_pdr_trigger_option FOREIGN KEY (trigger_option_id) REFERENCES parameter_option (id),
    CONSTRAINT fk_pdr_target_definition FOREIGN KEY (target_definition_id) REFERENCES parameter_definition (id),
    CONSTRAINT chk_pdr_source_not_target CHECK (source_definition_id <> target_definition_id)
) ENGINE=InnoDB;
CREATE INDEX idx_step_recipe_id ON step (recipe_id);
CREATE INDEX idx_parameter_option_definition_id ON parameter_option (definition_id);
CREATE INDEX idx_step_parameter_step_id ON step_parameter (step_id);
CREATE INDEX idx_step_parameter_definition_id ON step_parameter (definition_id);
CREATE INDEX idx_step_parameter_parent_id ON step_parameter (parent_step_parameter_id);
CREATE INDEX idx_step_parameter_selected_option_id ON step_parameter (selected_option_id);
CREATE INDEX idx_pdr_source_definition_id ON parameter_dependency_rule (source_definition_id);
CREATE INDEX idx_pdr_trigger_option_id ON parameter_dependency_rule (trigger_option_id);
CREATE INDEX idx_pdr_target_definition_id ON parameter_dependency_rule (target_definition_id);
ALTER TABLE recipe
    ADD CONSTRAINT chk_recipe_derived_parent
    CHECK (recipe_kind <> 'DERIVED' OR parent_recipe_id IS NOT NULL);
ALTER TABLE step
    ADD CONSTRAINT chk_step_kind_order
    CHECK (
        (step_kind = 'PRESTEP' AND order_index = 0)
        OR (step_kind = 'STEP' AND order_index >= 1)
    );
DROP TRIGGER IF EXISTS trg_step_parameter_bi;
DROP TRIGGER IF EXISTS trg_step_parameter_bu;
DROP TRIGGER IF EXISTS trg_pdr_bi;
DROP TRIGGER IF EXISTS trg_pdr_bu;
DELIMITER $$

CREATE TRIGGER trg_step_parameter_bi
BEFORE INSERT ON step_parameter
FOR EACH ROW
BEGIN
    DECLARE v_value_type VARCHAR(32);
DECLARE v_option_definition_id BIGINT;
IF NEW.parent_step_parameter_id IS NULL THEN
        SET NEW.parent_order_scope = 0;
ELSE
        SET NEW.parent_order_scope = NEW.parent_step_parameter_id;
END IF;
SELECT pd.value_type
      INTO v_value_type
      FROM parameter_definition pd
     WHERE pd.id = NEW.definition_id;
IF v_value_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid definition_id in step_parameter.';
END IF;
IF NEW.selected_option_id IS NOT NULL THEN
        IF v_value_type <> 'ENUM' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'selected_option_id is only valid for ENUM parameters.';
END IF;
SELECT po.definition_id
          INTO v_option_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.selected_option_id;
IF v_option_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid selected_option_id in step_parameter.';
END IF;
IF v_option_definition_id <> NEW.definition_id THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'selected_option_id must belong to definition_id.';
END IF;
END IF;
IF NEW.value_json IS NOT NULL AND v_value_type = 'ENUM' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ENUM parameters must use selected_option_id, not value_json.';
END IF;
END$$

CREATE TRIGGER trg_step_parameter_bu
BEFORE UPDATE ON step_parameter
FOR EACH ROW
BEGIN
    DECLARE v_value_type VARCHAR(32);
DECLARE v_option_definition_id BIGINT;
IF NEW.parent_step_parameter_id IS NULL THEN
        SET NEW.parent_order_scope = 0;
ELSE
        SET NEW.parent_order_scope = NEW.parent_step_parameter_id;
END IF;
SELECT pd.value_type
      INTO v_value_type
      FROM parameter_definition pd
     WHERE pd.id = NEW.definition_id;
IF v_value_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid definition_id in step_parameter.';
END IF;
IF NEW.selected_option_id IS NOT NULL THEN
        IF v_value_type <> 'ENUM' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'selected_option_id is only valid for ENUM parameters.';
END IF;
SELECT po.definition_id
          INTO v_option_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.selected_option_id;
IF v_option_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid selected_option_id in step_parameter.';
END IF;
IF v_option_definition_id <> NEW.definition_id THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'selected_option_id must belong to definition_id.';
END IF;
END IF;
IF NEW.value_json IS NOT NULL AND v_value_type = 'ENUM' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ENUM parameters must use selected_option_id, not value_json.';
END IF;
END$$

CREATE TRIGGER trg_pdr_bi
BEFORE INSERT ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_option_definition_id BIGINT;
SELECT po.definition_id
      INTO v_option_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;
IF v_option_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
END IF;
IF v_option_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
END IF;
END$$

CREATE TRIGGER trg_pdr_bu
BEFORE UPDATE ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_option_definition_id BIGINT;
SELECT po.definition_id
      INTO v_option_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;
IF v_option_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
END IF;
IF v_option_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
END IF;
END$$

DELIMITER;
ALTER TABLE parameter_definition
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;
ALTER TABLE parameter_definition
    ADD CONSTRAINT uq_parameter_definition_code UNIQUE (code);
ALTER TABLE parameter_definition
    MODIFY COLUMN code VARCHAR(128) NOT NULL;
ALTER TABLE step
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;
ALTER TABLE step
    ADD CONSTRAINT uq_step_recipe_code UNIQUE (recipe_id, code);
ALTER TABLE step
    MODIFY COLUMN code VARCHAR(128) NOT NULL;
ALTER TABLE parameter_option
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;
ALTER TABLE parameter_option
    ADD CONSTRAINT uq_po_definition_code UNIQUE (definition_id, code);
ALTER TABLE parameter_option
    MODIFY COLUMN code VARCHAR(128) NOT NULL;
CREATE INDEX idx_parameter_definition_code ON parameter_definition (code);
CREATE INDEX idx_step_recipe_code ON step (recipe_id, code);
CREATE INDEX idx_parameter_option_definition_code ON parameter_option (definition_id, code);
ALTER TABLE parameter_definition
    ADD COLUMN is_for_prestep TINYINT(1) NOT NULL DEFAULT 0 AFTER required_on_step;
ALTER TABLE parameter_definition
    ADD COLUMN step_type VARCHAR(16) NOT NULL DEFAULT 'STEP' AFTER required_on_step;
ALTER TABLE parameter_definition
    DROP COLUMN is_for_prestep;
ALTER TABLE parameter_dependency_rule
    ADD COLUMN required_source_activation_option_id BIGINT NULL;
ALTER TABLE parameter_dependency_rule
    ADD CONSTRAINT fk_pdr_required_source_activation_option
    FOREIGN KEY (required_source_activation_option_id)
    REFERENCES parameter_option (id);
CREATE INDEX idx_pdr_required_source_activation_option_id
    ON parameter_dependency_rule (required_source_activation_option_id);
CREATE TABLE decision_execution (
    id BIGINT NOT NULL AUTO_INCREMENT,
    result_profile_id BIGINT NOT NULL,
    validated_golden_recipe_id BIGINT NOT NULL,
    selected_machine_id BIGINT NOT NULL,
    created_derived_recipe_id BIGINT NULL,
    creator_id BIGINT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_de_result_profile
        FOREIGN KEY (result_profile_id) REFERENCES decision_result_profile(id),
    CONSTRAINT fk_de_validated_golden_recipe
        FOREIGN KEY (validated_golden_recipe_id) REFERENCES recipe(id),
    CONSTRAINT fk_de_created_derived_recipe
        FOREIGN KEY (created_derived_recipe_id) REFERENCES recipe(id)
) ENGINE=InnoDB;
CREATE TABLE decision_execution_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    decision_execution_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dea_execution
        FOREIGN KEY (decision_execution_id) REFERENCES decision_execution(id),
    CONSTRAINT fk_dea_question
        FOREIGN KEY (question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_dea_option
        FOREIGN KEY (option_id) REFERENCES decision_option(id),
    CONSTRAINT uq_dea_execution_question UNIQUE (decision_execution_id, question_id)
) ENGINE=InnoDB;
CREATE INDEX idx_de_result_profile_id ON decision_execution (result_profile_id);
CREATE INDEX idx_de_validated_golden_recipe_id ON decision_execution (validated_golden_recipe_id);
CREATE INDEX idx_de_created_derived_recipe_id ON decision_execution (created_derived_recipe_id);
CREATE INDEX idx_dea_execution_id ON decision_execution_answer (decision_execution_id);
CREATE INDEX idx_dea_question_id ON decision_execution_answer (question_id);
CREATE INDEX idx_dea_option_id ON decision_execution_answer (option_id);
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
ALTER TABLE parameter_definition
    DROP INDEX uq_parameter_definition_name,
    DROP INDEX uq_parameter_definition_alias,
    DROP INDEX uq_parameter_definition_code,
    ADD CONSTRAINT uq_parameter_definition_step_code UNIQUE (step_type, code);
ALTER TABLE parameter_definition
    ADD CONSTRAINT uq_parameter_definition_step_name UNIQUE (step_type, name),
    ADD CONSTRAINT uq_parameter_definition_step_alias UNIQUE (step_type, alias);
CREATE TABLE recipe_required_capability (
    recipe_id BIGINT NOT NULL,
    capability_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (recipe_id, capability_id),
    CONSTRAINT fk_rrc_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rrc_capability
        FOREIGN KEY (capability_id)
        REFERENCES chamber_capability(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE recipe_required_configuration (
    recipe_id BIGINT NOT NULL,
    configuration_definition_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (recipe_id, configuration_definition_id),
    CONSTRAINT fk_rrcfg_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rrcfg_configuration_definition
        FOREIGN KEY (configuration_definition_id)
        REFERENCES configuration_definition(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_rrc_capability_id ON recipe_required_capability (capability_id);
CREATE INDEX idx_rrcfg_definition_id ON recipe_required_configuration (configuration_definition_id);
ALTER TABLE parameter_definition
ADD COLUMN configuration_definition_id BIGINT UNSIGNED NULL,
ADD INDEX idx_parameter_definition_configuration_definition_id (configuration_definition_id),
ADD CONSTRAINT fk_parameter_definition_configuration_definition
    FOREIGN KEY (configuration_definition_id)
    REFERENCES configuration_definition(id)
    ON DELETE SET NULL;
ALTER TABLE parameter_definition
    ADD COLUMN IF NOT EXISTS parameter_group VARCHAR(128) NULL AFTER default_value_json,
    ADD COLUMN IF NOT EXISTS parameter_group_order INT NOT NULL DEFAULT 0 AFTER parameter_group;
CREATE INDEX IF NOT EXISTS idx_parameter_definition_group
    ON parameter_definition (step_type, parameter_group, parameter_group_order, name);
ALTER TABLE step_parameter
    ADD COLUMN user_modified BIT(1) NOT NULL DEFAULT b'0' AFTER locked_by_golden;
-- Expand dependency-rule uniqueness so the same source/trigger/target can exist in multiple activation contexts.
ALTER TABLE parameter_dependency_rule
    DROP INDEX IF EXISTS uq_pdr_source_trigger_target;
SET @pdr_constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'parameter_dependency_rule'
      AND constraint_name = 'uq_pdr_source_trigger_required_target'
);
SET @pdr_constraint_sql := IF(
    @pdr_constraint_exists = 0,
    'ALTER TABLE parameter_dependency_rule ADD CONSTRAINT uq_pdr_source_trigger_required_target UNIQUE (source_definition_id, trigger_option_id, required_source_activation_option_id, target_definition_id)',
    'SELECT 1'
);
PREPARE pdr_constraint_stmt FROM @pdr_constraint_sql;
EXECUTE pdr_constraint_stmt;
DEALLOCATE PREPARE pdr_constraint_stmt;
DELIMITER $$

DROP TRIGGER IF EXISTS trg_pdr_bi$$
CREATE TRIGGER trg_pdr_bi
BEFORE INSERT ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_trigger_definition_id BIGINT;
DECLARE v_required_definition_id BIGINT;
DECLARE v_allowed_controller_count BIGINT;
SELECT po.definition_id
      INTO v_trigger_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;
IF v_trigger_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
END IF;
IF v_trigger_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
END IF;
IF NEW.required_source_activation_option_id IS NOT NULL THEN
        SELECT po.definition_id
          INTO v_required_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.required_source_activation_option_id;
IF v_required_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid required_source_activation_option_id in parameter_dependency_rule.';
END IF;
IF v_required_definition_id <> NEW.source_definition_id THEN
            SELECT COUNT(*)
              INTO v_allowed_controller_count
              FROM parameter_dependency_rule pdr
             WHERE pdr.target_definition_id = NEW.source_definition_id
               AND pdr.source_definition_id = v_required_definition_id;
IF v_allowed_controller_count = 0 THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'Required source activation option must belong to source definition or controlling definition.';
END IF;
END IF;
END IF;
END$$

DROP TRIGGER IF EXISTS trg_pdr_bu$$
CREATE TRIGGER trg_pdr_bu
BEFORE UPDATE ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_trigger_definition_id BIGINT;
DECLARE v_required_definition_id BIGINT;
DECLARE v_allowed_controller_count BIGINT;
SELECT po.definition_id
      INTO v_trigger_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;
IF v_trigger_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
END IF;
IF v_trigger_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
END IF;
IF NEW.required_source_activation_option_id IS NOT NULL THEN
        SELECT po.definition_id
          INTO v_required_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.required_source_activation_option_id;
IF v_required_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid required_source_activation_option_id in parameter_dependency_rule.';
END IF;
IF v_required_definition_id <> NEW.source_definition_id THEN
            SELECT COUNT(*)
              INTO v_allowed_controller_count
              FROM parameter_dependency_rule pdr
             WHERE pdr.target_definition_id = NEW.source_definition_id
               AND pdr.source_definition_id = v_required_definition_id;
IF v_allowed_controller_count = 0 THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'Required source activation option must belong to source definition or controlling definition.';
END IF;
END IF;
END IF;
END$$

DELIMITER;
create table parameter_group (
    id bigint not null auto_increment,
    name varchar(128) not null,
    step_type varchar(32) not null,
    order_index int not null,
    create_time datetime(6) not null,
    revise_time datetime(6) not null,
    primary key (id),
    constraint uq_parameter_group_step_type_name unique (step_type, name),
    constraint uq_parameter_group_step_type_order unique (step_type, order_index)
);
alter table parameter_definition
    add column parameter_group_id bigint null,
    add column order_index_in_group int not null default 0;
alter table parameter_definition
    add constraint fk_parameter_definition_group
    foreign key (parameter_group_id) references parameter_group(id);
-- 1) Normalize and deduplicate legacy group names first
create temporary table tmp_group_source as
select
    pd.step_type as step_type,
    trim(pd.parameter_group) as group_name,
    min(coalesce(pd.parameter_group_order, 0)) as legacy_group_order
from parameter_definition pd
where pd.parameter_group is not null
  and trim(pd.parameter_group) <> ''
group by
    pd.step_type,
    trim(pd.parameter_group);
-- 2) Compute a unique stable order per step_type
create temporary table tmp_parameter_groups as
select
    src.step_type,
    src.group_name,
    row_number() over (
        partition by src.step_type
        order by src.legacy_group_order, src.group_name
    ) - 1 as computed_order
from tmp_group_source src;
-- 3) Stable order inside each group
create temporary table tmp_definition_rank as
select
    pd.id,
    row_number() over (
        partition by pd.parameter_group_id
        order by pd.name, pd.id
    ) - 1 as computed_order
from parameter_definition pd
where pd.parameter_group_id is not null;
drop temporary table if exists tmp_definition_rank;
drop temporary table if exists tmp_parameter_groups;
drop temporary table if exists tmp_group_source;
alter table parameter_definition
    add constraint uq_parameter_definition_group_order
    unique (parameter_group_id, order_index_in_group);
ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_step_type
    CHECK (step_type IN ('STEP', 'PRESTEP', 'ENDPOINT'));
ALTER TABLE parameter_group
    ADD CONSTRAINT chk_parameter_group_step_type
    CHECK (step_type IN ('STEP', 'PRESTEP', 'ENDPOINT'));
CREATE TABLE IF NOT EXISTS step_endpoint (
    id BIGINT NOT NULL AUTO_INCREMENT,
    step_id BIGINT NOT NULL,
    clause VARCHAR(8) NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_step_endpoint_step UNIQUE (step_id),
    CONSTRAINT fk_step_endpoint_step FOREIGN KEY (step_id) REFERENCES step (id),
    CONSTRAINT chk_step_endpoint_clause CHECK (clause IS NULL OR clause IN ('AND', 'OR'))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS step_endpoint_condition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    endpoint_id BIGINT NOT NULL,
    endpoint_parameter_id BIGINT NOT NULL,
    value_json LONGTEXT NULL,
    selected_option_id BIGINT NULL,
    operator VARCHAR(8) NOT NULL,
    order_index INT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_endpoint_condition_order UNIQUE (endpoint_id, order_index),
    CONSTRAINT fk_endpoint_condition_endpoint FOREIGN KEY (endpoint_id) REFERENCES step_endpoint (id),
    CONSTRAINT fk_endpoint_condition_parameter FOREIGN KEY (endpoint_parameter_id) REFERENCES parameter_definition (id),
    CONSTRAINT fk_endpoint_condition_selected_option FOREIGN KEY (selected_option_id) REFERENCES parameter_option (id),
    CONSTRAINT chk_endpoint_condition_xor CHECK ((value_json IS NULL) <> (selected_option_id IS NULL)),
    CONSTRAINT chk_endpoint_condition_operator CHECK (operator IN ('EQ', 'NEQ', 'LT', 'LTE', 'GT', 'GTE'))
) ENGINE=InnoDB;
CREATE INDEX idx_step_endpoint_step_id ON step_endpoint (step_id);
CREATE INDEX idx_endpoint_condition_endpoint_id ON step_endpoint_condition (endpoint_id);
CREATE INDEX idx_endpoint_condition_parameter_id ON step_endpoint_condition (endpoint_parameter_id);
CREATE INDEX idx_endpoint_condition_selected_option_id ON step_endpoint_condition (selected_option_id);
ALTER TABLE step_endpoint
    ADD COLUMN locked_by_golden TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE recipe
    ADD COLUMN wafer VARCHAR(32) NOT NULL DEFAULT 'PRESENT',
    ADD COLUMN iapc VARCHAR(32) NOT NULL DEFAULT 'NO',
    ADD COLUMN resumable VARCHAR(32) NOT NULL DEFAULT 'NO',
    ADD COLUMN chamber_type VARCHAR(255) NULL,
    ADD COLUMN access_display_groups VARCHAR(255) NULL,
    ADD COLUMN access_modify_groups VARCHAR(255) NULL,
    ADD COLUMN uda_file VARCHAR(255) NULL,
    ADD COLUMN type VARCHAR(64) NULL,
    ADD COLUMN max_time INT NULL,
    ADD COLUMN template VARCHAR(255) NULL;
ALTER TABLE recipe
    ADD CONSTRAINT chk_recipe_wafer_mode
    CHECK (wafer IN ('PRESENT', 'ABSENT', 'DONT_CARE')),
    ADD CONSTRAINT chk_recipe_iapc_mode
    CHECK (iapc IN ('NO', 'YES', 'REQUIRED', 'REQUIRED_IF_NO_HOST')),
    ADD CONSTRAINT chk_recipe_resumable_mode
    CHECK (resumable IN ('YES', 'NO')),
    ADD CONSTRAINT chk_recipe_max_time
    CHECK (max_time IS NULL OR max_time >= 0);
ALTER TABLE parameter_definition
    ADD COLUMN xml_section VARCHAR(32) NOT NULL DEFAULT 'REGULAR' AFTER step_type;
ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_xml_section
        CHECK (xml_section IN ('REGULAR', 'PASSPORT'));
ALTER TABLE recipe
    ALTER COLUMN access_display_groups SET DEFAULT 'ALL',
    ALTER COLUMN access_modify_groups SET DEFAULT 'ALL';
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
ALTER TABLE parameter_definition
    DROP CONSTRAINT chk_parameter_definition_xml_section;
ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_xml_section
        CHECK (xml_section IN ('REGULAR', 'GAS_PANEL', 'STEP_ATTRIBUTE'));
ALTER TABLE parameter_group
    ADD COLUMN is_system_group TINYINT(1) NOT NULL DEFAULT 0 AFTER order_index;
CREATE TEMPORARY TABLE tmp_step_types AS
SELECT 'STEP' AS step_type
UNION
SELECT 'PRESTEP' AS step_type
UNION
SELECT 'ENDPOINT' AS step_type
UNION
SELECT DISTINCT step_type FROM parameter_definition
UNION
SELECT DISTINCT step_type FROM parameter_group;
CREATE TEMPORARY TABLE tmp_null_definition_assignment AS
SELECT
    pd.id AS definition_id,
    ug.id AS ungrouped_group_id
FROM parameter_definition pd
JOIN parameter_group ug
  ON ug.step_type = pd.step_type
 AND ug.is_system_group = 1
WHERE pd.parameter_group_id IS NULL;
CREATE TEMPORARY TABLE tmp_ungrouped_existing_max AS
SELECT
    ug.id AS ungrouped_group_id,
    COALESCE(MAX(pd.order_index_in_group), -1) AS max_order
FROM parameter_group ug
LEFT JOIN parameter_definition pd
  ON pd.parameter_group_id = ug.id
WHERE ug.is_system_group = 1
GROUP BY ug.id;
CREATE TEMPORARY TABLE tmp_null_definition_rank AS
SELECT
    assign.definition_id,
    assign.ungrouped_group_id,
    ROW_NUMBER() OVER (
        PARTITION BY assign.ungrouped_group_id
        ORDER BY pd.name, pd.id
    ) - 1 AS rank_in_group
FROM tmp_null_definition_assignment assign
JOIN parameter_definition pd ON pd.id = assign.definition_id;
DROP TEMPORARY TABLE IF EXISTS tmp_null_definition_rank;
DROP TEMPORARY TABLE IF EXISTS tmp_ungrouped_existing_max;
DROP TEMPORARY TABLE IF EXISTS tmp_null_definition_assignment;
DROP TEMPORARY TABLE IF EXISTS tmp_step_types;
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
