START TRANSACTION;

-- Process level question: replace Critique/Non critique labels with the new process-level choices.
UPDATE decision_option o
JOIN decision_question q ON q.id = o.question_id
SET o.label = 'Pas dans la liste',
    o.value = 'NON_CRITIQUE',
    o.order_index = 1,
    o.revise_time = NOW(6)
WHERE q.code = 'PROCESS_LEVEL'
  AND o.value = 'NON_CRITIQUE';

UPDATE decision_option o
JOIN decision_question q ON q.id = o.question_id
SET o.label = 'GO1',
    o.value = 'GO1',
    o.order_index = 2,
    o.revise_time = NOW(6)
WHERE q.code = 'PROCESS_LEVEL'
  AND o.value = 'CRITIQUE';

INSERT INTO decision_option (question_id, label, value, order_index, create_time, revise_time)
SELECT q.id, 'GO2', 'GO2', 3, NOW(6), NOW(6)
FROM decision_question q
WHERE q.code = 'PROCESS_LEVEL'
  AND NOT EXISTS (
      SELECT 1
      FROM decision_option o
      WHERE o.question_id = q.id
        AND o.value = 'GO2'
  );

INSERT INTO decision_option (question_id, label, value, order_index, create_time, revise_time)
SELECT q.id, 'GO3', 'GO3', 4, NOW(6), NOW(6)
FROM decision_question q
WHERE q.code = 'PROCESS_LEVEL'
  AND NOT EXISTS (
      SELECT 1
      FROM decision_option o
      WHERE o.question_id = q.id
        AND o.value = 'GO3'
  );

INSERT INTO decision_option (question_id, label, value, order_index, create_time, revise_time)
SELECT q.id, 'HVO', 'HVO', 5, NOW(6), NOW(6)
FROM decision_question q
WHERE q.code = 'PROCESS_LEVEL'
  AND NOT EXISTS (
      SELECT 1
      FROM decision_option o
      WHERE o.question_id = q.id
        AND o.value = 'HVO'
  );

INSERT INTO decision_option (question_id, label, value, order_index, create_time, revise_time)
SELECT q.id, 'OXID_GATE_CTRENCH', 'OXID_GATE_CTRENCH', 6, NOW(6), NOW(6)
FROM decision_question q
WHERE q.code = 'PROCESS_LEVEL'
  AND NOT EXISTS (
      SELECT 1
      FROM decision_option o
      WHERE o.question_id = q.id
        AND o.value = 'OXID_GATE_CTRENCH'
  );

INSERT INTO decision_option (question_id, label, value, order_index, create_time, revise_time)
SELECT q.id, 'OXID_NVM', 'OXID_NVM', 7, NOW(6), NOW(6)
FROM decision_question q
WHERE q.code = 'PROCESS_LEVEL'
  AND NOT EXISTS (
      SELECT 1
      FROM decision_option o
      WHERE o.question_id = q.id
        AND o.value = 'OXID_NVM'
  );

-- Remove stale transition for the legacy Critique option if it still exists under another value.
DELETE t
FROM decision_transition t
JOIN decision_option o ON o.id = t.option_id
JOIN decision_question q ON q.id = t.current_question_id
WHERE q.code = 'PROCESS_LEVEL'
  AND o.value = 'CRITIQUE';

-- Existing non-critical path remains unchanged.
UPDATE decision_transition t
JOIN decision_option o ON o.id = t.option_id
JOIN decision_question q ON q.id = t.current_question_id
SET t.next_question_id = (
        SELECT nq.id FROM decision_question nq WHERE nq.code = 'RESISTIVITY_LOW_TEMP_NON_CRITIQUE' LIMIT 1
    ),
    t.revise_time = NOW(6)
WHERE q.code = 'PROCESS_LEVEL'
  AND o.value = 'NON_CRITIQUE';

-- Newly inserted process levels follow the same path as the former Critique option.
INSERT INTO decision_transition (current_question_id, option_id, next_question_id, result_profile_id, create_time, revise_time)
SELECT q.id, o.id,
       (SELECT nq.id FROM decision_question nq WHERE nq.code = 'RESISTIVITY_LOW_TEMP_CRITIQUE' LIMIT 1),
       NULL, NOW(6), NOW(6)
FROM decision_question q
JOIN decision_option o ON o.question_id = q.id
WHERE q.code = 'PROCESS_LEVEL'
  AND o.value IN ('GO1', 'GO2', 'GO3', 'HVO', 'OXID_GATE_CTRENCH', 'OXID_NVM')
  AND NOT EXISTS (
      SELECT 1
      FROM decision_transition t
      WHERE t.current_question_id = q.id
        AND t.option_id = o.id
  );

-- Resistivity options: standardize the labels shown in the wizard.
UPDATE decision_option o
JOIN decision_question q ON q.id = o.question_id
SET o.label = 'Substrate Resistivity>10mOhm.cm',
    o.revise_time = NOW(6)
WHERE q.code IN ('RESISTIVITY_HIGH_TEMP', 'RESISTIVITY_LOW_TEMP_NON_CRITIQUE', 'RESISTIVITY_LOW_TEMP_CRITIQUE')
  AND o.label = '> N valeur';

UPDATE decision_option o
JOIN decision_question q ON q.id = o.question_id
SET o.label = CONCAT('Substrate Resistivity', CHAR(226,137,164 USING utf8mb4), '10mOhm.cm'),
    o.revise_time = NOW(6)
WHERE q.code IN ('RESISTIVITY_HIGH_TEMP', 'RESISTIVITY_LOW_TEMP_NON_CRITIQUE', 'RESISTIVITY_LOW_TEMP_CRITIQUE')
  AND o.label = '<= N valeur';

UPDATE decision_option o
JOIN decision_question q ON q.id = o.question_id
SET o.label = 'Substrate Resistivity<=10mOhm.cm',
    o.revise_time = NOW(6)
WHERE q.code IN ('RESISTIVITY_HIGH_TEMP', 'RESISTIVITY_LOW_TEMP_NON_CRITIQUE', 'RESISTIVITY_LOW_TEMP_CRITIQUE')
  AND o.label LIKE 'Substrate Resistivity%10mOhm.cm'
  AND o.order_index = 2;

COMMIT;