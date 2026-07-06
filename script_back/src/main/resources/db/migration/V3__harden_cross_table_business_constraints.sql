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

DELIMITER ;
