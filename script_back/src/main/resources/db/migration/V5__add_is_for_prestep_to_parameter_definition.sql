ALTER TABLE parameter_definition
    ADD COLUMN is_for_prestep TINYINT(1) NOT NULL DEFAULT 0 AFTER required_on_step;
