-- Expand dependency-rule uniqueness so the same source/trigger/target can exist in multiple activation contexts.
ALTER TABLE parameter_dependency_rule
    DROP INDEX IF EXISTS uq_pdr_source_trigger_target;

SET @pdr_constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'parameter_dependency_rule'
      AND constraint_name = 'uq_pdr_source_trigger_required_target'
);

SET @pdr_constraint_sql := IF(
    @pdr_constraint_exists = 0,
    'ALTER TABLE parameter_dependency_rule ADD CONSTRAINT uq_pdr_source_trigger_required_target UNIQUE (source_definition_id, trigger_option_id, required_source_activation_option_id, target_definition_id)',
    'SELECT 1'
);

PREPARE pdr_constraint_stmt FROM @pdr_constraint_sql;
EXECUTE pdr_constraint_stmt;
DEALLOCATE PREPARE pdr_constraint_stmt;

-- Seed the RTC_MODE -> temperature/voltage dependency rules.
-- Codes are normalized from the business names/labels used by the application.

-- RTC_MODE -> temperature parameters
INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       NULL,
       target_definition.id,
       'DISABLE',
       'STEP',
       100,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'TEMP_TARGET'
WHERE source_definition.code = 'RTC_MODE'
  AND trigger_option.code IN ('CONST_VOLTAGE', 'IDLE', 'RAMP_VOLTAGE')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id IS NULL
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       NULL,
       target_definition.id,
       'DISABLE',
       'STEP',
       100,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'TEMP_RAMP_RATE'
WHERE source_definition.code = 'RTC_MODE'
  AND trigger_option.code IN ('CONST_TEMP', 'STABILIZE_TEMP', 'CONST_VOLTAGE', 'IDLE', 'RAMP_VOLTAGE')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id IS NULL
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       NULL,
       target_definition.id,
       'DISABLE',
       'STEP',
       100,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'TEMP_START'
WHERE source_definition.code = 'RTC_MODE'
  AND trigger_option.code IN ('CONST_TEMP', 'STABILIZE_TEMP', 'CONST_VOLTAGE', 'IDLE', 'RAMP_VOLTAGE')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id IS NULL
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       NULL,
       target_definition.id,
       'DISABLE',
       'STEP',
       100,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_CONTROL'
WHERE source_definition.code = 'RTC_MODE'
  AND trigger_option.code IN ('CONST_TEMP', 'STABILIZE_TEMP', 'RAMP_TEMP', 'IDLE')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id IS NULL
        AND existing.target_definition_id = target_definition.id
  );

-- VOLTAGE_CONTROL -> downstream voltage parameters when RTC_MODE = CONST_VOLTAGE
INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_TARGET'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'DESIGNATED_VALUES'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_RAMP_RATE'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code IN ('DESIGNATED_VALUES', 'USE_VLT_TARGET')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_START'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code IN ('DESIGNATED_VALUES', 'USE_VLT_TARGET')
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_TARGET'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'DESIGNATED_VALUES'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

-- Under CONST_VOLTAGE, DESIGNATED_VALUES only enables the explicit Voltage Zone definitions.
-- Under RAMP_VOLTAGE, DESIGNATED_VALUES already allows all downstream targets in this process tree.
-- Under RAMP_VOLTAGE, USE_VLT_TARGET disables the explicit Voltage Zone definitions.
INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_RAMP_RATE'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'DESIGNATED_VALUES'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_START'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'DESIGNATED_VALUES'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

-- Under CONST_VOLTAGE, USE_VLT_TARGET only enables VOLTAGE_TARGET.
INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code IN (
        'VOLTAGE_ZONE_1',
        'VOLTAGE_ZONE_2',
        'VOLTAGE_ZONE_3',
        'VOLTAGE_ZONE_4',
        'VOLTAGE_ZONE_5',
        'VOLTAGE_ZONE_6',
        'VOLTAGE_ZONE_7',
        'VOLTAGE_ZONE_8',
        'VOLTAGE_ZONE_9',
        'VOLTAGE_ZONE_10',
        'VOLTAGE_ZONE_11',
        'VOLTAGE_ZONE_12',
        'VOLTAGE_ZONE_13',
        'VOLTAGE_ZONE_14',
        'VOLTAGE_ZONE_15'
    )
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'USE_VLT_TARGET'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_RAMP_RATE'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'USE_VLT_TARGET'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code = 'VOLTAGE_START'
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'CONST_VOLTAGE'
  AND trigger_option.code = 'USE_VLT_TARGET'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );

INSERT INTO parameter_dependency_rule (
    source_definition_id,
    trigger_option_id,
    required_source_activation_option_id,
    target_definition_id,
    effect,
    scope,
    priority,
    create_time,
    revise_time
)
SELECT source_definition.id,
       trigger_option.id,
       required_option.id,
       target_definition.id,
       'DISABLE',
       'STEP',
       200,
       NOW(),
       NOW()
FROM parameter_definition source_definition
JOIN parameter_option trigger_option
    ON trigger_option.definition_id = source_definition.id
JOIN parameter_option required_option
    ON required_option.definition_id = source_definition.id
JOIN parameter_definition target_definition
    ON target_definition.code IN (
        'VOLTAGE_ZONE_1',
        'VOLTAGE_ZONE_2',
        'VOLTAGE_ZONE_3',
        'VOLTAGE_ZONE_4',
        'VOLTAGE_ZONE_5',
        'VOLTAGE_ZONE_6',
        'VOLTAGE_ZONE_7',
        'VOLTAGE_ZONE_8',
        'VOLTAGE_ZONE_9',
        'VOLTAGE_ZONE_10',
        'VOLTAGE_ZONE_11',
        'VOLTAGE_ZONE_12',
        'VOLTAGE_ZONE_13',
        'VOLTAGE_ZONE_14',
        'VOLTAGE_ZONE_15'
    )
WHERE source_definition.code = 'VOLTAGE_CONTROL'
  AND required_option.code = 'RAMP_VOLTAGE'
  AND trigger_option.code = 'USE_VLT_TARGET'
  AND NOT EXISTS (
      SELECT 1
      FROM parameter_dependency_rule existing
      WHERE existing.source_definition_id = source_definition.id
        AND existing.trigger_option_id = trigger_option.id
        AND existing.required_source_activation_option_id = required_option.id
        AND existing.target_definition_id = target_definition.id
  );
