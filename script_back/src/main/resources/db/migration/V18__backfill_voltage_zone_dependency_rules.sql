-- Backfill the Voltage Zone dependency rules that were added to V17 after this database was already migrated.
-- This migration is intentionally additive and idempotent.

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