START TRANSACTION;

SET @step_id := (
  SELECT id FROM step WHERE recipe_id = 6 AND step_kind = 'PRESTEP' LIMIT 1
);

INSERT INTO step_parameter (
  step_id, definition_id, parent_step_parameter_id, parent_order_scope,
  order_index, label_override, value_json, selected_option_id,
  activation_state, locked_by_golden, create_time, revise_time
)
SELECT
  @step_id,
  m.definition_id,
  NULL,
  0,
  m.ord - 1,
  NULL,
  CASE WHEN m.value_type = 'ENUM' THEN NULL ELSE JSON_QUOTE(m.pvalue) END,
  CASE WHEN m.value_type = 'ENUM' THEN m.selected_option_id ELSE NULL END,
  'ENABLED',
  b'0',
  NOW(6),
  NOW(6)
FROM (
    SELECT
      x.ord,
      x.alias,
      x.pvalue,
      pd.id AS definition_id,
      pd.value_type,
      COALESCE(
        MAX(CASE WHEN po.label = x.pvalue OR po.code = x.pvalue THEN po.id END),
        MAX(CASE WHEN x.alias = 'EntryPowerOn' AND po.code = 'PINS_RAISED' THEN po.id END),
        MAX(CASE WHEN x.alias = 'RTC_TempCtrl' AND (po.code LIKE '26\\_%' OR po.label LIKE '26:%') THEN po.id END)
      ) AS selected_option_id
    FROM (
      SELECT 1 AS ord, 'RTC_TempCtrl' AS alias, 'Controller26' AS pvalue UNION ALL
      SELECT 2, 'PreExec_RcpName', 'PREHEAT PAD 100T' UNION ALL
      SELECT 3, 'PreExec_RcpTime', '120' UNION ALL
      SELECT 4, 'Neutr_RcpName', '' UNION ALL
      SELECT 5, 'Neutr_Trigger', 'Skip if same recipe' UNION ALL
      SELECT 6, 'LampOut_Table', '' UNION ALL
      SELECT 7, 'Temp_PeakIntlk', 'No' UNION ALL
      SELECT 8, 'Temp_MeanUpper', '0.0' UNION ALL
      SELECT 9, 'Temp_MeanLower', '0.0' UNION ALL
      SELECT 10, 'Temp_SharpnessUpper', '0.000' UNION ALL
      SELECT 11, 'Temp_SharpnessLower', '0.000' UNION ALL
      SELECT 12, 'HdrP1MNOMMode', 'Use Default' UNION ALL
      SELECT 13, 'Temp_Probe1', 'No' UNION ALL
      SELECT 14, 'Temp_Probe2', 'No' UNION ALL
      SELECT 15, 'Temp_Probe3', 'No' UNION ALL
      SELECT 16, 'Temp_Probe4', 'No' UNION ALL
      SELECT 17, 'Temp_Probe5', 'No' UNION ALL
      SELECT 18, 'Temp_Probe6', 'No' UNION ALL
      SELECT 19, 'Temp_Probe7', 'No' UNION ALL
      SELECT 20, 'TempOffsetTable', 'RTO 1100C' UNION ALL
      SELECT 21, 'TempOff_Offset01', '0' UNION ALL
      SELECT 22, 'TempOff_Offset02', '0' UNION ALL
      SELECT 23, 'TempOff_Offset03', '0' UNION ALL
      SELECT 24, 'TempOff_Offset04', '0' UNION ALL
      SELECT 25, 'TempOff_Offset05', '0' UNION ALL
      SELECT 26, 'TempOff_Offset06', '0' UNION ALL
      SELECT 27, 'TempOff_Offset07', '0' UNION ALL
      SELECT 28, 'TempOff_Offset08', '0' UNION ALL
      SELECT 29, 'TempOffsetTable2', 'ADD-OFFSETS-I1050-75A' UNION ALL
      SELECT 30, 'Uni_Ctrl', 'Disable' UNION ALL
      SELECT 31, 'Uni_1stIndex', '0' UNION ALL
      SELECT 32, 'Uni_FaultTol', '0.0' UNION ALL
      SELECT 33, 'Uni_WarnTol', '0.0' UNION ALL
      SELECT 34, 'PresOffsetTable', '' UNION ALL
      SELECT 35, 'HdrO2An_CtrlMode', 'Disable' UNION ALL
      SELECT 36, 'HdrO2An_WarnLevel', '0.0' UNION ALL
      SELECT 37, 'HdrO2An_FaultLevel', '0.0' UNION ALL
      SELECT 38, 'HdrO2An_FlowLow', '0' UNION ALL
      SELECT 39, 'HdrO2An_FlowHigh', '0' UNION ALL
      SELECT 40, 'EntryPowerOn', 'EntryPowerOnPinsRaised' UNION ALL
      SELECT 41, 'Preheat_Time', '0' UNION ALL
      SELECT 42, 'LoadDelay_Time', '0.0' UNION ALL
      SELECT 43, 'VoltPre_Zone01', '5' UNION ALL
      SELECT 44, 'VoltPre_Zone02', '5' UNION ALL
      SELECT 45, 'VoltPre_Zone03', '5' UNION ALL
      SELECT 46, 'VoltPre_Zone04', '5' UNION ALL
      SELECT 47, 'VoltPre_Zone05', '5' UNION ALL
      SELECT 48, 'VoltPre_Zone06', '5' UNION ALL
      SELECT 49, 'VoltPre_Zone07', '5' UNION ALL
      SELECT 50, 'VoltPre_Zone08', '5' UNION ALL
      SELECT 51, 'VoltPre_Zone09', '5' UNION ALL
      SELECT 52, 'VoltPre_Zone10', '5' UNION ALL
      SELECT 53, 'VoltPre_Zone11', '5' UNION ALL
      SELECT 54, 'VoltPre_Zone12', '5' UNION ALL
      SELECT 55, 'VoltPre_Zone13', '5' UNION ALL
      SELECT 56, 'VoltPre_Zone14', '5' UNION ALL
      SELECT 57, 'VoltPre_Zone15', '5' UNION ALL
      SELECT 58, 'MagLev_HomeRotCmd', 'Enable'
    ) x
    JOIN parameter_definition pd
      ON pd.step_type = 'PRESTEP'
     AND pd.alias = x.alias
    LEFT JOIN parameter_option po
      ON po.definition_id = pd.id
    GROUP BY x.ord, x.alias, x.pvalue, pd.id, pd.value_type
) m
LEFT JOIN step_parameter sp
  ON sp.step_id = @step_id
 AND sp.parent_order_scope = 0
 AND sp.definition_id = m.definition_id
WHERE sp.id IS NULL
  AND (m.value_type <> 'ENUM' OR m.selected_option_id IS NOT NULL);

SELECT ROW_COUNT() AS inserted_step_params;
SELECT @step_id AS prestep_id;
SELECT COUNT(*) AS total_params_on_prestep
FROM step_parameter
WHERE step_id = @step_id;

SELECT sp.order_index, pd.alias, pd.name, pd.value_type, sp.value_json, sp.selected_option_id
FROM step_parameter sp
JOIN parameter_definition pd ON pd.id = sp.definition_id
WHERE sp.step_id = @step_id
ORDER BY sp.order_index;

COMMIT;
