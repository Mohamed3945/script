DELIMITER $$

DROP TRIGGER IF EXISTS trg_pdr_bi$$
CREATE TRIGGER trg_pdr_bi
BEFORE INSERT ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_trigger_definition_id BIGINT;
    DECLARE v_required_definition_id BIGINT;
    DECLARE v_allowed_controller_count BIGINT;

    SELECT po.definition_id
      INTO v_trigger_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;

    IF v_trigger_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
    END IF;

    IF v_trigger_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
    END IF;

    IF NEW.required_source_activation_option_id IS NOT NULL THEN
        SELECT po.definition_id
          INTO v_required_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.required_source_activation_option_id;

        IF v_required_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid required_source_activation_option_id in parameter_dependency_rule.';
        END IF;

        IF v_required_definition_id <> NEW.source_definition_id THEN
            SELECT COUNT(*)
              INTO v_allowed_controller_count
              FROM parameter_dependency_rule pdr
             WHERE pdr.target_definition_id = NEW.source_definition_id
               AND pdr.source_definition_id = v_required_definition_id;

            IF v_allowed_controller_count = 0 THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'Required source activation option must belong to source definition or controlling definition.';
            END IF;
        END IF;
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_pdr_bu$$
CREATE TRIGGER trg_pdr_bu
BEFORE UPDATE ON parameter_dependency_rule
FOR EACH ROW
BEGIN
    DECLARE v_trigger_definition_id BIGINT;
    DECLARE v_required_definition_id BIGINT;
    DECLARE v_allowed_controller_count BIGINT;

    SELECT po.definition_id
      INTO v_trigger_definition_id
      FROM parameter_option po
     WHERE po.id = NEW.trigger_option_id;

    IF v_trigger_definition_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid trigger_option_id in parameter_dependency_rule.';
    END IF;

    IF v_trigger_definition_id <> NEW.source_definition_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Trigger option must belong to source definition.';
    END IF;

    IF NEW.required_source_activation_option_id IS NOT NULL THEN
        SELECT po.definition_id
          INTO v_required_definition_id
          FROM parameter_option po
         WHERE po.id = NEW.required_source_activation_option_id;

        IF v_required_definition_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid required_source_activation_option_id in parameter_dependency_rule.';
        END IF;

        IF v_required_definition_id <> NEW.source_definition_id THEN
            SELECT COUNT(*)
              INTO v_allowed_controller_count
              FROM parameter_dependency_rule pdr
             WHERE pdr.target_definition_id = NEW.source_definition_id
               AND pdr.source_definition_id = v_required_definition_id;

            IF v_allowed_controller_count = 0 THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'Required source activation option must belong to source definition or controlling definition.';
            END IF;
        END IF;
    END IF;
END$$

DELIMITER ;