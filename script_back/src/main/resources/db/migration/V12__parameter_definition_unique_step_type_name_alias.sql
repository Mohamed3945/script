ALTER TABLE parameter_definition
    ADD CONSTRAINT uq_parameter_definition_step_name UNIQUE (step_type, name),
    ADD CONSTRAINT uq_parameter_definition_step_alias UNIQUE (step_type, alias);
