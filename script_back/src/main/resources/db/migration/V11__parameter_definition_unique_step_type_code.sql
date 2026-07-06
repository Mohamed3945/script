ALTER TABLE parameter_definition
    DROP INDEX uq_parameter_definition_name,
    DROP INDEX uq_parameter_definition_alias,
    DROP INDEX uq_parameter_definition_code,
    ADD CONSTRAINT uq_parameter_definition_step_code UNIQUE (step_type, code);
