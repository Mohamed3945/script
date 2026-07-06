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
