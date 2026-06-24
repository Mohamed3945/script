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
