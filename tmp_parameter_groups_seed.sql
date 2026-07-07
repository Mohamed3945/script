ALTER TABLE parameter_definition
  ADD COLUMN IF NOT EXISTS parameter_group VARCHAR(128) NULL AFTER default_value_json,
  ADD COLUMN IF NOT EXISTS parameter_group_order INT NOT NULL DEFAULT 0 AFTER parameter_group;

CREATE INDEX IF NOT EXISTS idx_parameter_definition_group
  ON parameter_definition (step_type, parameter_group, parameter_group_order, name);

UPDATE parameter_definition
SET parameter_group = 'Peak Temperature Limits',
    parameter_group_order = 10
WHERE step_type = 'PRESTEP'
  AND alias IN ('Temp_MeanUpper', 'Temp_MeanLower');

UPDATE parameter_definition
SET parameter_group = 'Temperature Probes',
    parameter_group_order = 20
WHERE step_type = 'PRESTEP'
  AND alias IN ('Temp_Probe1', 'Temp_Probe2', 'Temp_Probe3', 'Temp_Probe4', 'Temp_Probe5', 'Temp_Probe6', 'Temp_Probe7');

UPDATE parameter_definition
SET parameter_group = 'Temperature Offset',
    parameter_group_order = 30
WHERE step_type = 'PRESTEP'
  AND alias IN (
    'TempOffsetTable', 'TempOffsetTable2',
    'TempOff_Offset01', 'TempOff_Offset02', 'TempOff_Offset03', 'TempOff_Offset04',
    'TempOff_Offset05', 'TempOff_Offset06', 'TempOff_Offset07', 'TempOff_Offset08'
  );

UPDATE parameter_definition
SET parameter_group = 'Voltage',
    parameter_group_order = 10
WHERE step_type = 'STEP'
  AND alias IN ('Volt_Ctrl', 'Volt_Target', 'Volt_Start', 'Volt_RampRate');

UPDATE parameter_definition
SET parameter_group = 'Voltage Zone',
    parameter_group_order = 20
WHERE step_type = 'STEP'
  AND alias IN (
    'Volt_Zone01', 'Volt_Zone02', 'Volt_Zone03', 'Volt_Zone04', 'Volt_Zone05',
    'Volt_Zone06', 'Volt_Zone07', 'Volt_Zone08', 'Volt_Zone09', 'Volt_Zone10',
    'Volt_Zone11', 'Volt_Zone12', 'Volt_Zone13', 'Volt_Zone14', 'Volt_Zone15'
  );

UPDATE parameter_definition
SET parameter_group = 'Average Vcmd',
    parameter_group_order = 30
WHERE step_type = 'STEP'
  AND alias IN (
    'Vcmd_GroupMax1', 'Vcmd_GroupMax2', 'Vcmd_GroupMax3', 'Vcmd_GroupMax4',
    'Vcmd_GroupMax5', 'Vcmd_GroupMax6', 'Vcmd_GroupMax7',
    'Vcmd_GroupMin1', 'Vcmd_GroupMin2', 'Vcmd_GroupMin3', 'Vcmd_GroupMin4',
    'Vcmd_GroupMin5', 'Vcmd_GroupMin6', 'Vcmd_GroupMin7'
  );

UPDATE parameter_definition
SET parameter_group = 'Mean Temp Calculation',
    parameter_group_order = 40
WHERE step_type = 'STEP'
  AND alias IN (
    'Temp_IncTemp1', 'Temp_IncTemp2', 'Temp_IncTemp3', 'Temp_IncTemp4',
    'Temp_IncTemp5', 'Temp_IncTemp6', 'Temp_IncTemp7', 'Temp_IncTemp8'
  );

UPDATE parameter_definition
SET parameter_group = 'Temp Offsets',
    parameter_group_order = 50
WHERE step_type = 'STEP'
  AND alias IN (
    'Step_TempOffset01', 'Step_TempOffset02', 'Step_TempOffset03', 'Step_TempOffset04',
    'Step_TempOffset05', 'Step_TempOffset06', 'Step_TempOffset07', 'Step_TempOffset08'
  );

UPDATE parameter_definition
SET parameter_group = 'O2 Analyzer',
    parameter_group_order = 60
WHERE step_type = 'STEP'
  AND alias IN ('O2An_CtrlMode', 'O2An_FaultLevel', 'O2An_WarnLevel');

UPDATE parameter_definition
SET parameter_group = 'Gaspanel',
    parameter_group_order = 70
WHERE step_type = 'STEP'
  AND alias IN (
    'H2HIGH_Flow_Ramp', 'H2HIGH_Flow_Set',
    'N2_Flow_Ramp', 'N2_Flow_Set',
    'O2HIGH_Flow_Ramp', 'O2HIGH_Flow_Set'
  );

SELECT step_type, parameter_group, parameter_group_order, COUNT(*) AS cnt
FROM parameter_definition
WHERE step_type IN ('PRESTEP', 'STEP')
  AND parameter_group IS NOT NULL
GROUP BY step_type, parameter_group, parameter_group_order
ORDER BY step_type, parameter_group_order, parameter_group;
