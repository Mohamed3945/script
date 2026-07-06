ALTER TABLE parameter_definition
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;

UPDATE parameter_definition
SET code = UPPER(REPLACE(TRIM(name), ' ', '_'))
WHERE code IS NULL OR code = '';

ALTER TABLE parameter_definition
    ADD CONSTRAINT uq_parameter_definition_code UNIQUE (code);

ALTER TABLE parameter_definition
    MODIFY COLUMN code VARCHAR(128) NOT NULL;

ALTER TABLE step
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;

UPDATE step
SET code = CONCAT('STEP_', order_index)
WHERE code IS NULL OR code = '';

ALTER TABLE step
    ADD CONSTRAINT uq_step_recipe_code UNIQUE (recipe_id, code);

ALTER TABLE step
    MODIFY COLUMN code VARCHAR(128) NOT NULL;

ALTER TABLE parameter_option
    ADD COLUMN IF NOT EXISTS code VARCHAR(128) NULL;

UPDATE parameter_option
SET code = UPPER(REPLACE(TRIM(label), ' ', '_'))
WHERE code IS NULL OR code = '';

UPDATE parameter_option po
JOIN (
    SELECT definition_id, code
    FROM parameter_option
    GROUP BY definition_id, code
    HAVING COUNT(*) > 1
) dup ON dup.definition_id = po.definition_id AND dup.code = po.code
SET po.code = CONCAT(po.code, '_', po.id);

ALTER TABLE parameter_option
    ADD CONSTRAINT uq_po_definition_code UNIQUE (definition_id, code);

ALTER TABLE parameter_option
    MODIFY COLUMN code VARCHAR(128) NOT NULL;

CREATE INDEX idx_parameter_definition_code ON parameter_definition (code);
CREATE INDEX idx_step_recipe_code ON step (recipe_id, code);
CREATE INDEX idx_parameter_option_definition_code ON parameter_option (definition_id, code);