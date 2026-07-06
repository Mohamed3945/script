ALTER TABLE parameter_definition
    ADD COLUMN step_type VARCHAR(16) NOT NULL DEFAULT 'STEP' AFTER required_on_step;

UPDATE parameter_definition
SET step_type = CASE WHEN is_for_prestep = 1 THEN 'PRESTEP' ELSE 'STEP' END;

ALTER TABLE parameter_definition
    DROP COLUMN is_for_prestep;
