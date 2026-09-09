ALTER TABLE parameter_group
    ADD COLUMN is_system_group TINYINT(1) NOT NULL DEFAULT 0 AFTER order_index;

-- If an Ungrouped group already exists for a step type, mark it as the system group.
UPDATE parameter_group
SET is_system_group = 1
WHERE LOWER(TRIM(name)) = 'ungrouped';

CREATE TEMPORARY TABLE tmp_step_types AS
SELECT 'STEP' AS step_type
UNION
SELECT 'PRESTEP' AS step_type
UNION
SELECT 'ENDPOINT' AS step_type
UNION
SELECT DISTINCT step_type FROM parameter_definition
UNION
SELECT DISTINCT step_type FROM parameter_group;

-- Create missing system Ungrouped groups per step type.
INSERT INTO parameter_group (name, step_type, order_index, is_system_group, create_time, revise_time)
SELECT
    'Ungrouped',
    ts.step_type,
    COALESCE((SELECT MAX(pg.order_index) + 1 FROM parameter_group pg WHERE pg.step_type = ts.step_type), 0),
    1,
    NOW(6),
    NOW(6)
FROM tmp_step_types ts
WHERE NOT EXISTS (
    SELECT 1
    FROM parameter_group pg
    WHERE pg.step_type = ts.step_type
      AND pg.is_system_group = 1
);

CREATE TEMPORARY TABLE tmp_null_definition_assignment AS
SELECT
    pd.id AS definition_id,
    ug.id AS ungrouped_group_id
FROM parameter_definition pd
JOIN parameter_group ug
  ON ug.step_type = pd.step_type
 AND ug.is_system_group = 1
WHERE pd.parameter_group_id IS NULL;

CREATE TEMPORARY TABLE tmp_ungrouped_existing_max AS
SELECT
    ug.id AS ungrouped_group_id,
    COALESCE(MAX(pd.order_index_in_group), -1) AS max_order
FROM parameter_group ug
LEFT JOIN parameter_definition pd
  ON pd.parameter_group_id = ug.id
WHERE ug.is_system_group = 1
GROUP BY ug.id;

CREATE TEMPORARY TABLE tmp_null_definition_rank AS
SELECT
    assign.definition_id,
    assign.ungrouped_group_id,
    ROW_NUMBER() OVER (
        PARTITION BY assign.ungrouped_group_id
        ORDER BY pd.name, pd.id
    ) - 1 AS rank_in_group
FROM tmp_null_definition_assignment assign
JOIN parameter_definition pd ON pd.id = assign.definition_id;

UPDATE parameter_definition pd
JOIN tmp_null_definition_rank ranked ON ranked.definition_id = pd.id
JOIN tmp_ungrouped_existing_max mx ON mx.ungrouped_group_id = ranked.ungrouped_group_id
SET pd.parameter_group_id = ranked.ungrouped_group_id,
    pd.order_index_in_group = mx.max_order + 1 + ranked.rank_in_group;

DROP TEMPORARY TABLE IF EXISTS tmp_null_definition_rank;
DROP TEMPORARY TABLE IF EXISTS tmp_ungrouped_existing_max;
DROP TEMPORARY TABLE IF EXISTS tmp_null_definition_assignment;
DROP TEMPORARY TABLE IF EXISTS tmp_step_types;
