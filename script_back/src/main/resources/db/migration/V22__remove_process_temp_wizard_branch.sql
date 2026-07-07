START TRANSACTION;

-- Redirect PROCESS_TYPE -> ISSG directly to PROCESS_LEVEL.
UPDATE decision_transition t
JOIN decision_question q ON q.id = t.current_question_id
JOIN decision_option o ON o.id = t.option_id
SET t.next_question_id = (
        SELECT nq.id FROM decision_question nq WHERE nq.code = 'PROCESS_LEVEL' LIMIT 1
    ),
    t.revise_time = NOW(6)
WHERE q.code = 'PROCESS_TYPE'
  AND o.value = 'ISSG';

-- Remove the obsolete PROCESS_TEMP branch transitions.
DELETE t
FROM decision_transition t
JOIN decision_question q ON q.id = t.current_question_id
WHERE q.code = 'PROCESS_TEMP';

-- Deactivate the obsolete question so it no longer appears in the wizard flow.
UPDATE decision_question
SET active = b'0',
    revise_time = NOW(6)
WHERE code = 'PROCESS_TEMP';

COMMIT;