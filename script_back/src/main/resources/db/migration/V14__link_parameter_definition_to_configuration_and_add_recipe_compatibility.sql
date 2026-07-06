ALTER TABLE parameter_definition
ADD COLUMN configuration_definition_id BIGINT UNSIGNED NULL,
ADD INDEX idx_parameter_definition_configuration_definition_id (configuration_definition_id),
ADD CONSTRAINT fk_parameter_definition_configuration_definition
    FOREIGN KEY (configuration_definition_id)
    REFERENCES configuration_definition(id)
    ON DELETE SET NULL;
