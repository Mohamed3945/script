ALTER TABLE parameter_definition
    ADD COLUMN IF NOT EXISTS parameter_group VARCHAR(128) NULL AFTER default_value_json,
    ADD COLUMN IF NOT EXISTS parameter_group_order INT NOT NULL DEFAULT 0 AFTER parameter_group;

CREATE INDEX IF NOT EXISTS idx_parameter_definition_group
    ON parameter_definition (step_type, parameter_group, parameter_group_order, name);
