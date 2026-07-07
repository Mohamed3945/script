-- Rebuild recipe id 8 with latest matrix model, parameter definitions, groups, and ordering.
-- Target DB: MariaDB/MySQL (run in DBeaver)
-- IMPORTANT:
-- 1) This script deletes recipe id 8 if it exists.
-- 2) It recreates recipe 8 as DERIVED from golden recipe id 1.
-- 3) It creates/updates latest STEP parameter definitions (Name/Chambers/Mode/Max Time)
--    and applies machine-style group ordering.
-- 4) It instantiates step_parameter rows for all step definitions on each STEP/PRESTEP,
--    with locked_by_golden=true and user_modified=false by default.

START TRANSACTION;

SET @target_recipe_id = 8;
SET @golden_recipe_id = 1;

-- Safety check: golden must exist
SELECT id, name, recipe_kind
FROM recipe
WHERE id = @golden_recipe_id;

-- =====================================================================
-- 0) Cleanup old recipe 8 if present
-- =====================================================================
UPDATE decision_execution
SET created_derived_recipe_id = NULL
WHERE created_derived_recipe_id = @target_recipe_id;

DELETE sp
FROM step_parameter sp
JOIN step s ON s.id = sp.step_id
WHERE s.recipe_id = @target_recipe_id;

DELETE FROM step WHERE recipe_id = @target_recipe_id;
DELETE FROM recipe_requirement WHERE recipe_id = @target_recipe_id;
DELETE FROM recipe_compatibility WHERE recipe_id = @target_recipe_id;
DELETE FROM recipe WHERE id = @target_recipe_id;

-- =====================================================================
-- 1) Ensure latest STEP parameter definitions exist
-- =====================================================================
-- Name
INSERT INTO parameter_definition (
    code, name, alias, unit, description, value_type, required_on_step,
    step_type, default_value_json, parameter_group, parameter_group_order,
    configuration_definition_id, create_time, revise_time
)
SELECT
    'NAME', 'Name', 'NAME', NULL, NULL, 'STRING', b'0',
    'STEP', NULL, NULL, 999,
    NULL, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Name'
);

-- Chambers
INSERT INTO parameter_definition (
    code, name, alias, unit, description, value_type, required_on_step,
    step_type, default_value_json, parameter_group, parameter_group_order,
    configuration_definition_id, create_time, revise_time
)
SELECT
    'CHAMBERS', 'Chambers', 'CHAMBERS', NULL, NULL, 'ENUM', b'0',
    'STEP', NULL, NULL, 999,
    NULL, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Chambers'
);

-- Mode
INSERT INTO parameter_definition (
    code, name, alias, unit, description, value_type, required_on_step,
    step_type, default_value_json, parameter_group, parameter_group_order,
    configuration_definition_id, create_time, revise_time
)
SELECT
    'MODE', 'Mode', 'MODE', NULL, NULL, 'ENUM', b'0',
    'STEP', NULL, NULL, 999,
    NULL, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Mode'
);

-- Max Time
INSERT INTO parameter_definition (
    code, name, alias, unit, description, value_type, required_on_step,
    step_type, default_value_json, parameter_group, parameter_group_order,
    configuration_definition_id, create_time, revise_time
)
SELECT
    'MAX_TIME', 'Max Time', 'MAX_TIME', NULL, NULL, 'NUMBER', b'0',
    'STEP', '"0"', NULL, 999,
    NULL, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Max Time'
);

-- =====================================================================
-- 2) Ensure enum options for Mode / Chambers
-- =====================================================================
SET @def_mode_id = (
    SELECT id FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Mode' LIMIT 1
);
SET @def_chambers_id = (
    SELECT id FROM parameter_definition WHERE step_type = 'STEP' AND name = 'Chambers' LIMIT 1
);

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_mode_id, 'TIME', 'Time', 0, NOW(6), NOW(6)
WHERE @def_mode_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_mode_id AND label = 'Time');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_mode_id, 'ENDPOINT', 'Endpoint', 1, NOW(6), NOW(6)
WHERE @def_mode_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_mode_id AND label = 'Endpoint');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_mode_id, 'TIMEORENDPOINT', 'TimeOrEndpoint', 2, NOW(6), NOW(6)
WHERE @def_mode_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_mode_id AND label = 'TimeOrEndpoint');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_chambers_id, 'CHA', 'CHA', 0, NOW(6), NOW(6)
WHERE @def_chambers_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_chambers_id AND label = 'CHA');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_chambers_id, 'CHB', 'CHB', 1, NOW(6), NOW(6)
WHERE @def_chambers_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_chambers_id AND label = 'CHB');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_chambers_id, 'CHC', 'CHC', 2, NOW(6), NOW(6)
WHERE @def_chambers_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_chambers_id AND label = 'CHC');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_chambers_id, 'CHD', 'CHD', 3, NOW(6), NOW(6)
WHERE @def_chambers_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_chambers_id AND label = 'CHD');

INSERT INTO parameter_option (definition_id, code, label, order_index, create_time, revise_time)
SELECT @def_chambers_id, 'CHA_CHD', 'CHA:CHD', 4, NOW(6), NOW(6)
WHERE @def_chambers_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM parameter_option WHERE definition_id = @def_chambers_id AND label = 'CHA:CHD');

-- =====================================================================
-- 3) Apply machine-style group names and order for STEP definitions
-- =====================================================================
UPDATE parameter_definition 
SET
    parameter_group = CASE
        WHEN name IN ('Temp Target', 'Temp Start', 'Temp Ramp Rate') THEN 'Temps Params'
        WHEN name IN ('Temp Offset 1', 'Temp Offset 2', 'Temp Offset 3', 'Temp Offset 4',
                      'Temp Offset 5', 'Temp Offset 6', 'Temp Offset 7', 'Temp Offset 8', 'Offset Table') THEN 'Temp Offsets'
        WHEN name IN ('Voltage Control', 'Voltage Target', 'Voltage Start', 'Voltage Ramp Rate') THEN 'Voltage'
        WHEN name LIKE 'Voltage Zone %' THEN 'Voltage Zones'
        WHEN name LIKE 'Lamp Group %' THEN 'Average Vcmd'
        WHEN name IN ('Range Check', 'Range Check Vcmd usage') THEN 'Average Vcmd'
        WHEN name IN ('Press Mode', 'Press Target', 'Press Ramp Rate', 'Apply Press Offset',
                      'Position Target', 'Position Ramp Rate') THEN 'PVC params'
        WHEN name IN ('Lift Pins Pos', 'MagLev Control', 'Rotation Speed') THEN 'Lift&Rotation'
        WHEN name IN ('O2 Step Ctrl', 'O2 Warning Level', 'O2 Fault Level') THEN 'O2 Analyzer'
        WHEN name IN ('Emissivity Min Lim', 'Emissivity Max Lim') THEN 'Emissivity'
        WHEN name LIKE 'Include Temp % in Calc' THEN 'Mean Temp Calculation'
        WHEN (
            (UPPER(name) LIKE '%H2HIGH%' OR UPPER(alias) LIKE '%H2HIGH%'
             OR UPPER(name) LIKE '%O2HIGH%' OR UPPER(alias) LIKE '%O2HIGH%'
             OR UPPER(name) LIKE '%N2%' OR UPPER(alias) LIKE '%N2%')
            AND
            (UPPER(name) LIKE '%FLOW%' OR UPPER(alias) LIKE '%FLOW%'
             OR UPPER(name) LIKE '%RAMP%' OR UPPER(alias) LIKE '%RAMP%')
        ) THEN 'Gaspannel'
        WHEN name IN ('RPS Power Setpoint') THEN 'RPS Power Setpoint'
        WHEN name IN ('Endpoint') THEN 'Endpoint'
        ELSE parameter_group
    END,
    parameter_group_order = CASE
        WHEN name IN ('Temp Target', 'Temp Start', 'Temp Ramp Rate') THEN 10
        WHEN name IN ('Temp Offset 1', 'Temp Offset 2', 'Temp Offset 3', 'Temp Offset 4',
                      'Temp Offset 5', 'Temp Offset 6', 'Temp Offset 7', 'Temp Offset 8') THEN 20
        WHEN name IN ('Voltage Control', 'Voltage Target', 'Voltage Start', 'Voltage Ramp Rate') THEN 30
        WHEN name LIKE 'Voltage Zone %' THEN 40
        WHEN name LIKE 'Lamp Group %' THEN 50
        WHEN name IN ('Press Mode', 'Press Target', 'Press Ramp Rate', 'Apply Press Offset',
                      'Position Target', 'Position Ramp Rate') THEN 60
        WHEN name IN ('Lift Pins Pos', 'MagLev Control', 'Rotation Speed') THEN 70
        WHEN name IN ('O2 Step Ctrl', 'O2 Warning Level', 'O2 Fault Level') THEN 80
        WHEN name IN ('Emissivity Min Lim', 'Emissivity Max Lim') THEN 90
        WHEN name LIKE 'Include Temp % in Calc' THEN 100
        WHEN (
            (UPPER(name) LIKE '%H2HIGH%' OR UPPER(alias) LIKE '%H2HIGH%'
             OR UPPER(name) LIKE '%O2HIGH%' OR UPPER(alias) LIKE '%O2HIGH%'
             OR UPPER(name) LIKE '%N2%' OR UPPER(alias) LIKE '%N2%')
            AND
            (UPPER(name) LIKE '%FLOW%' OR UPPER(alias) LIKE '%FLOW%'
             OR UPPER(name) LIKE '%RAMP%' OR UPPER(alias) LIKE '%RAMP%')
        ) THEN 110
        WHEN name IN ('Range Check', 'Range Check Vcmd usage') THEN 120
        WHEN name IN ('RPS Power Setpoint') THEN 130
        ELSE parameter_group_order
    END,
    revise_time = NOW(6)
WHERE step_type = 'STEP';

-- =====================================================================
-- 4) Recreate recipe id 8 as DERIVED from golden id 1
-- =====================================================================
INSERT INTO recipe (
    id, recipe_kind, parent_recipe_id, name, description,
    creator, revisor, process_family, status, version, frozen,
    create_time, revise_time
)
SELECT
    @target_recipe_id,
    'DERIVED',
    r.id,
    CONCAT(r.name, ' - Derived'),
    r.description,
    COALESCE(r.creator, 1),
    NULL,
    r.process_family,
    'DRAFT',
    COALESCE((SELECT MAX(x.version) + 1 FROM recipe x WHERE x.parent_recipe_id = r.id), 1),
    b'0',
    NOW(6),
    NOW(6)
FROM recipe r
WHERE r.id = @golden_recipe_id;

-- Create steps from XML model (15 STEP + 1 PRESTEP)
INSERT INTO step (recipe_id, step_kind, order_index, name, code, create_time, revise_time)
SELECT @target_recipe_id, s.step_kind, s.order_index, s.name, s.code, NOW(6), NOW(6)
FROM (
    SELECT 0 AS order_index, 'PRESTEP' AS step_kind, 'PRESTEP' AS name, 'PRESTEP' AS code
    UNION ALL SELECT 1 AS order_index, 'STEP' AS step_kind, 'HOP' AS name, 'STEP_01' AS code
    UNION ALL SELECT 2 AS order_index, 'STEP' AS step_kind, 'RESET GAS INTERLOCK' AS name, 'STEP_02' AS code
    UNION ALL SELECT 3 AS order_index, 'STEP' AS step_kind, 'P-STABILIZE' AS name, 'STEP_03' AS code
    UNION ALL SELECT 4 AS order_index, 'STEP' AS step_kind, 'P-STABILIZE' AS name, 'STEP_04' AS code
    UNION ALL SELECT 5 AS order_index, 'STEP' AS step_kind, 'TIME LOOP' AS name, 'STEP_05' AS code
    UNION ALL SELECT 6 AS order_index, 'STEP' AS step_kind, 'OPEN LOOP' AS name, 'STEP_06' AS code
    UNION ALL SELECT 7 AS order_index, 'STEP' AS step_kind, 'SOR' AS name, 'STEP_07' AS code
    UNION ALL SELECT 8 AS order_index, 'STEP' AS step_kind, 'STAB' AS name, 'STEP_08' AS code
    UNION ALL SELECT 9 AS order_index, 'STEP' AS step_kind, 'FAST RAMP' AS name, 'STEP_09' AS code
    UNION ALL SELECT 10 AS order_index, 'STEP' AS step_kind, 'SOAK-A' AS name, 'STEP_10' AS code
    UNION ALL SELECT 11 AS order_index, 'STEP' AS step_kind, 'SOAK-D' AS name, 'STEP_11' AS code
    UNION ALL SELECT 12 AS order_index, 'STEP' AS step_kind, 'RAMP DOWN' AS name, 'STEP_12' AS code
    UNION ALL SELECT 13 AS order_index, 'STEP' AS step_kind, 'RESET GAS AND COOL' AS name, 'STEP_13' AS code
    UNION ALL SELECT 14 AS order_index, 'STEP' AS step_kind, 'SET XFER' AS name, 'STEP_14' AS code
    UNION ALL SELECT 15 AS order_index, 'STEP' AS step_kind, 'COOL' AS name, 'STEP_15' AS code
) s
ORDER BY s.order_index;

-- =====================================================================
-- 5) Clone all step parameters from golden to recipe 8
--    This preserves full cardinality and values from the source recipe.
-- =====================================================================
DROP TEMPORARY TABLE IF EXISTS tmp_step_map;
CREATE TEMPORARY TABLE tmp_step_map (
    old_step_id BIGINT NOT NULL PRIMARY KEY,
    new_step_id BIGINT NOT NULL
);

INSERT INTO tmp_step_map (old_step_id, new_step_id)
SELECT
    s_old.id,
    s_new.id
FROM step s_old
JOIN step s_new
    ON s_new.recipe_id = @target_recipe_id
   AND s_new.step_kind = s_old.step_kind
   AND s_new.order_index = s_old.order_index
   AND COALESCE(s_new.name, '') = COALESCE(s_old.name, '')
   AND COALESCE(s_new.code, '') = COALESCE(s_old.code, '')
WHERE s_old.recipe_id = @golden_recipe_id;

INSERT INTO step_parameter (
    step_id,
    definition_id,
    parent_step_parameter_id,
    parent_order_scope,
    order_index,
    label_override,
    value_json,
    selected_option_id,
    activation_state,
    locked_by_golden,
    user_modified,
    create_time,
    revise_time
)
SELECT
    sm.new_step_id AS step_id,
    sp_old.definition_id,
    NULL AS parent_step_parameter_id,
    sp_old.parent_order_scope,
    sp_old.order_index,
    sp_old.label_override,
    sp_old.value_json,
    sp_old.selected_option_id,
    sp_old.activation_state,
    b'1' AS locked_by_golden,
    b'0' AS user_modified,
    NOW(6) AS create_time,
    NOW(6) AS revise_time
FROM step_parameter sp_old
JOIN tmp_step_map sm ON sm.old_step_id = sp_old.step_id
ORDER BY sm.new_step_id, sp_old.order_index, sp_old.id;

DROP TEMPORARY TABLE IF EXISTS tmp_sp_map;
CREATE TEMPORARY TABLE tmp_sp_map (
    old_sp_id BIGINT NOT NULL PRIMARY KEY,
    new_sp_id BIGINT NOT NULL
);

DROP TEMPORARY TABLE IF EXISTS tmp_old_sp_ranked;
CREATE TEMPORARY TABLE tmp_old_sp_ranked AS
SELECT
    sp_old.id AS old_sp_id,
    sm.new_step_id,
    sp_old.definition_id,
    sp_old.parent_order_scope,
    sp_old.order_index,
    COALESCE(sp_old.label_override, '') AS label_override_key,
    COALESCE(sp_old.value_json, '') AS value_json_key,
    COALESCE(sp_old.selected_option_id, -1) AS selected_option_key,
    sp_old.activation_state,
    ROW_NUMBER() OVER (
        PARTITION BY
            sm.new_step_id,
            sp_old.definition_id,
            sp_old.parent_order_scope,
            sp_old.order_index,
            COALESCE(sp_old.label_override, ''),
            COALESCE(sp_old.value_json, ''),
            COALESCE(sp_old.selected_option_id, -1),
            sp_old.activation_state
        ORDER BY sp_old.id
    ) AS rn
FROM step_parameter sp_old
JOIN tmp_step_map sm ON sm.old_step_id = sp_old.step_id;

DROP TEMPORARY TABLE IF EXISTS tmp_new_sp_ranked;
CREATE TEMPORARY TABLE tmp_new_sp_ranked AS
SELECT
    sp_new.id AS new_sp_id,
    sp_new.step_id AS new_step_id,
    sp_new.definition_id,
    sp_new.parent_order_scope,
    sp_new.order_index,
    COALESCE(sp_new.label_override, '') AS label_override_key,
    COALESCE(sp_new.value_json, '') AS value_json_key,
    COALESCE(sp_new.selected_option_id, -1) AS selected_option_key,
    sp_new.activation_state,
    ROW_NUMBER() OVER (
        PARTITION BY
            sp_new.step_id,
            sp_new.definition_id,
            sp_new.parent_order_scope,
            sp_new.order_index,
            COALESCE(sp_new.label_override, ''),
            COALESCE(sp_new.value_json, ''),
            COALESCE(sp_new.selected_option_id, -1),
            sp_new.activation_state
        ORDER BY sp_new.id
    ) AS rn
FROM step_parameter sp_new
JOIN step s_new ON s_new.id = sp_new.step_id
WHERE s_new.recipe_id = @target_recipe_id;

INSERT INTO tmp_sp_map (old_sp_id, new_sp_id)
SELECT
    o.old_sp_id,
    n.new_sp_id
FROM tmp_old_sp_ranked o
JOIN tmp_new_sp_ranked n
    ON n.new_step_id = o.new_step_id
   AND n.definition_id = o.definition_id
   AND n.parent_order_scope = o.parent_order_scope
   AND n.order_index = o.order_index
   AND n.label_override_key = o.label_override_key
   AND n.value_json_key = o.value_json_key
   AND n.selected_option_key = o.selected_option_key
   AND n.activation_state = o.activation_state
   AND n.rn = o.rn;

UPDATE step_parameter sp_new
JOIN tmp_sp_map map_child ON map_child.new_sp_id = sp_new.id
JOIN step_parameter sp_old ON sp_old.id = map_child.old_sp_id
LEFT JOIN tmp_sp_map map_parent ON map_parent.old_sp_id = sp_old.parent_step_parameter_id
SET sp_new.parent_step_parameter_id = map_parent.new_sp_id;

DROP TEMPORARY TABLE IF EXISTS tmp_old_sp_ranked;
DROP TEMPORARY TABLE IF EXISTS tmp_new_sp_ranked;
DROP TEMPORARY TABLE IF EXISTS tmp_sp_map;
DROP TEMPORARY TABLE IF EXISTS tmp_step_map;

-- =====================================================================
-- 6) Final safety normalization
-- =====================================================================
UPDATE step_parameter sp
JOIN step s ON s.id = sp.step_id
SET
    sp.locked_by_golden = b'1',
    sp.user_modified = b'0',
    sp.revise_time = NOW(6)
WHERE s.recipe_id = @target_recipe_id;

-- =====================================================================
-- 7) Verification outputs (for DBeaver)
-- =====================================================================
SELECT id, name, recipe_kind, parent_recipe_id, version, status
FROM recipe
WHERE id = @target_recipe_id;

SELECT step_kind, COUNT(*) AS step_count
FROM step
WHERE recipe_id = @target_recipe_id
GROUP BY step_kind;

SELECT d.step_type, COUNT(*) AS definition_count
FROM parameter_definition d
GROUP BY d.step_type;

SELECT d.name, d.parameter_group, d.parameter_group_order
FROM parameter_definition d
WHERE d.step_type = 'STEP'
ORDER BY
    CASE LOWER(d.name)
        WHEN 'name' THEN 1
        WHEN 'chambers' THEN 2
        WHEN 'mode' THEN 3
        WHEN 'max time' THEN 4
        WHEN 'rtc mode' THEN 5
        ELSE 1000
    END,
    d.parameter_group_order,
    d.name;

SELECT
    COUNT(*) AS step_param_total,
    SUM(CASE WHEN locked_by_golden = b'1' THEN 1 ELSE 0 END) AS locked_true,
    SUM(CASE WHEN locked_by_golden = b'0' THEN 1 ELSE 0 END) AS locked_false
FROM step_parameter sp
JOIN step s ON s.id = sp.step_id
WHERE s.recipe_id = @target_recipe_id;

COMMIT;
