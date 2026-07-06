-- Remove legacy WAIT values now that WAIT is no longer supported in code.
UPDATE step_parameter
SET activation_state = 'ENABLED'
WHERE activation_state = 'WAIT';

UPDATE parameter_dependency_rule
SET effect = 'ENABLE'
WHERE effect = 'WAIT';
