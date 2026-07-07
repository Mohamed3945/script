-- Rebuild recipe id 8 with latest matrix model, parameter definitions, groups, and ordering.
-- Target DB: MariaDB/MySQL (run in DBeaver)
-- IMPORTANT:
-- 1) This script deletes recipe id 8 if it exists.
-- 2) It recreates recipe 8 as GOLDEN from XML model (no cloning from another recipe).
-- 3) It creates/updates latest STEP parameter definitions (Name/Chambers/Mode/Max Time)
--    and applies machine-style group ordering.
-- 4) It instantiates step_parameter rows for XML-referenced definitions on each STEP/PRESTEP,
--    with locked_by_golden=true by default.

START TRANSACTION;

SET @target_recipe_id = 8;

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
DELETE FROM recipe_required_capability WHERE recipe_id = @target_recipe_id;
DELETE FROM recipe_required_configuration WHERE recipe_id = @target_recipe_id;
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
    'STEP', '0', NULL, 999,
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
-- 4) Recreate recipe id 8 as GOLDEN from XML model
-- =====================================================================
INSERT INTO recipe (
    id, recipe_kind, parent_recipe_id, name, description,
    creator, revisor, process_family, status, version, frozen,
    create_time, revise_time
)
VALUES (
    @target_recipe_id,
    'GOLDEN',
    NULL,
    'GOLDEN V5 Rad ISSG Non Critique OL Manuel 5 Torr',
    'Rebuilt from XML model and normalized definitions/groups',
    1,
    NULL,
    'RAD',
    'DRAFT',
    1,
    b'0',
    NOW(6),
    NOW(6)
);

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
-- 5) Create step parameters from XML-referenced definitions (no clone)
-- =====================================================================

-- STEP parameters: restrict to aliases known from XML STEP inventory + mandatory controls
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
    create_time,
    revise_time
)
SELECT
    s.id AS step_id,
    d.id AS definition_id,
    NULL AS parent_step_parameter_id,
    0 AS parent_order_scope,
    ROW_NUMBER() OVER (
        PARTITION BY s.id
        ORDER BY
            CASE LOWER(d.name)
                WHEN 'name' THEN 1
                WHEN 'chambers' THEN 2
                WHEN 'mode' THEN 3
                WHEN 'max time' THEN 4
                WHEN 'rtc mode' THEN 5
                ELSE 1000
            END,
            COALESCE(d.parameter_group_order, 999),
            d.name,
            d.id
    ) - 1 AS order_index,
    NULL AS label_override,
    CASE
        WHEN d.value_type = 'ENUM' THEN NULL
        WHEN d.name = 'Name' THEN JSON_QUOTE(s.name)
        WHEN d.value_type = 'NUMBER' THEN '0'
        WHEN d.value_type = 'BOOLEAN' THEN 'false'
        WHEN d.value_type = 'JSON' THEN '{}'
        ELSE '""'
    END AS value_json,
    CASE
        WHEN d.value_type = 'ENUM' THEN COALESCE(
            (
                SELECT po.id
                FROM parameter_option po
                WHERE po.definition_id = d.id
                  AND po.label = CASE d.name
                      WHEN 'Mode' THEN 'Time'
                      WHEN 'Chambers' THEN 'CHA:CHD'
                      ELSE NULL
                  END
                LIMIT 1
            ),
            (
                SELECT po2.id
                FROM parameter_option po2
                WHERE po2.definition_id = d.id
                ORDER BY po2.order_index, po2.id
                LIMIT 1
            )
        )
        ELSE NULL
    END AS selected_option_id,
    'ENABLED' AS activation_state,
    b'1' AS locked_by_golden,
    NOW(6) AS create_time,
    NOW(6) AS revise_time
FROM step s
JOIN parameter_definition d ON d.step_type = 'STEP'
WHERE s.recipe_id = @target_recipe_id
  AND s.step_kind = 'STEP'
  AND (
        d.name IN ('Name', 'Chambers', 'Mode', 'Max Time')
        OR d.alias IN (
            'Em_MaxLimit', 'Em_MinLimit', 'H2HIGH_Flow_Ramp', 'H2HIGH_Flow_Set', 'Lift_PinPos', 'MagLev_Speed', 'MagLev_SpeedCmd', 'N2_Flow_Ramp', 'N2_Flow_Set',
            'O2An_CtrlMode', 'O2An_FaultLevel', 'O2An_WarnLevel', 'O2HIGH_Flow_Ramp', 'O2HIGH_Flow_Set', 'OffsetTable', 'RPS_PowerSP', 'SelOxideDest',
            'Step_TempOffset01', 'Step_TempOffset02', 'Step_TempOffset03', 'Step_TempOffset04', 'Step_TempOffset05', 'Step_TempOffset06', 'Step_TempOffset07',
            'Step_TempOffset08', 'StepPressOffsetApply', 'Temp_IncTemp1', 'Temp_IncTemp2', 'Temp_IncTemp3', 'Temp_IncTemp4', 'Temp_IncTemp5', 'Temp_IncTemp6',
            'Temp_IncTemp7', 'Temp_IncTemp8', 'Temp_Mode', 'Temp_RampRate', 'Temp_Start', 'Temp_Target', 'TV_PosRR', 'TV_PosSP', 'TV_PressCmd', 'TV_PressRR',
            'TV_PressureSP', 'Vcmd_GroupMax1', 'Vcmd_GroupMax2', 'Vcmd_GroupMax3', 'Vcmd_GroupMax4', 'Vcmd_GroupMax5', 'Vcmd_GroupMax6', 'Vcmd_GroupMax7',
            'Vcmd_GroupMin1', 'Vcmd_GroupMin2', 'Vcmd_GroupMin3', 'Vcmd_GroupMin4', 'Vcmd_GroupMin5', 'Vcmd_GroupMin6', 'Vcmd_GroupMin7', 'Vcmd_RangeCheck',
            'Vcmd_RangeCheckUsage', 'Volt_Ctrl', 'Volt_RampRate', 'Volt_Start', 'Volt_Target', 'Volt_Zone01', 'Volt_Zone02', 'Volt_Zone03', 'Volt_Zone04',
            'Volt_Zone05', 'Volt_Zone06', 'Volt_Zone07', 'Volt_Zone08', 'Volt_Zone09', 'Volt_Zone10', 'Volt_Zone11', 'Volt_Zone12', 'Volt_Zone13', 'Volt_Zone14', 'Volt_Zone15'
        )
      );

-- PRESTEP parameters: restrict to aliases present in XML PRESTEP payload
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
    create_time,
    revise_time
)
SELECT
    s.id AS step_id,
    d.id AS definition_id,
    NULL AS parent_step_parameter_id,
    0 AS parent_order_scope,
    ROW_NUMBER() OVER (
        PARTITION BY s.id
        ORDER BY
            COALESCE(d.parameter_group_order, 999),
            d.name,
            d.id
    ) - 1 AS order_index,
    NULL AS label_override,
    CASE
        WHEN d.value_type = 'ENUM' THEN NULL
        WHEN d.value_type = 'NUMBER' THEN '0'
        WHEN d.value_type = 'BOOLEAN' THEN 'false'
        WHEN d.value_type = 'JSON' THEN '{}'
        ELSE '""'
    END AS value_json,
    CASE
        WHEN d.value_type = 'ENUM' THEN (
            SELECT po.id
            FROM parameter_option po
            WHERE po.definition_id = d.id
            ORDER BY po.order_index, po.id
            LIMIT 1
        )
        ELSE NULL
    END AS selected_option_id,
    'ENABLED' AS activation_state,
    b'1' AS locked_by_golden,
    NOW(6) AS create_time,
    NOW(6) AS revise_time
FROM step s
JOIN parameter_definition d ON d.step_type = 'PRESTEP'
WHERE s.recipe_id = @target_recipe_id
  AND s.step_kind = 'PRESTEP'
  AND d.alias IN (
      'RTC_TempCtrl', 'PreExec_RcpName', 'PreExec_RcpTime', 'Neutr_RcpName', 'Neutr_Trigger', 'LampOut_Table', 'Temp_PeakIntlk', 'Temp_MeanUpper',
      'Temp_MeanLower', 'Temp_SharpnessUpper', 'Temp_SharpnessLower', 'HdrP1MNOMMode', 'Temp_Probe1', 'Temp_Probe2', 'Temp_Probe3', 'Temp_Probe4',
      'Temp_Probe5', 'Temp_Probe6', 'Temp_Probe7', 'TempOffsetTable', 'TempOff_Offset01', 'TempOff_Offset02', 'TempOff_Offset03', 'TempOff_Offset04',
      'TempOff_Offset05', 'TempOff_Offset06', 'TempOff_Offset07', 'TempOff_Offset08', 'TempOffsetTable2', 'Uni_Ctrl', 'Uni_1stIndex', 'Uni_FaultTol',
      'Uni_WarnTol', 'PresOffsetTable', 'HdrO2An_CtrlMode', 'HdrO2An_WarnLevel', 'HdrO2An_FaultLevel', 'HdrO2An_FlowLow', 'HdrO2An_FlowHigh',
      'EntryPowerOn', 'Preheat_Time', 'LoadDelay_Time', 'VoltPre_Zone01', 'VoltPre_Zone02', 'VoltPre_Zone03', 'VoltPre_Zone04', 'VoltPre_Zone05',
      'VoltPre_Zone06', 'VoltPre_Zone07', 'VoltPre_Zone08', 'VoltPre_Zone09', 'VoltPre_Zone10', 'VoltPre_Zone11', 'VoltPre_Zone12', 'VoltPre_Zone13',
      'VoltPre_Zone14', 'VoltPre_Zone15', 'MagLev_HomeRotCmd'
  );

-- =====================================================================
-- 6) Final safety normalization
-- =====================================================================
UPDATE step_parameter sp
JOIN step s ON s.id = sp.step_id
SET
    sp.locked_by_golden = b'1',
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
