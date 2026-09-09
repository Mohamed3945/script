# Verification report recipe 6

Date: 2026-07-06
Source XML: GOLDEN V5 Rad ISSG Non Critique OL Manuel 5 Torr.xml
API checked: GET /api/recipes/6/matrix

## 1) Recipe/steps created
- Recipe 6 is reachable from API.
- Columns returned: 16 (1 PRESTEP + 15 STEP).
- STEP columns: STEP_01 to STEP_15 are present.

## 2) XML vs API parameter aliases (STEP scope)
- Distinct aliases in XML STEP block: 78
- Distinct aliases returned by matrix rows: 130
- Missing from API vs XML: 6
- Extra in API vs XML: 58

### Missing aliases (in XML, not found in matrix rows)
- H2HIGH_Flow_Set
- H2HIGH_Flow_Ramp
- O2HIGH_Flow_Set
- O2HIGH_Flow_Ramp
- N2_Flow_Set
- N2_Flow_Ramp

### Extra aliases (found in matrix rows, not part of XML STEP block)
Mainly PRESTEP-like families are included in STEP matrix rows, for example:
- RTC_TempCtrl
- PreExec_RcpName
- PreExec_RcpTime
- Temp_Probe1..Temp_Probe7
- VoltPre_Zone01..VoltPre_Zone15
- HdrO2An_*
- TempOffsetTable / TempOff_Offset*

## 3) Group metadata status
Matrix rows by group:
- (empty): 111
- Peak Temperature Limits: 2
- Temperature Offset: 10
- Temperature Probes: 7

Target STEP aliases expected to be grouped but currently empty:
- Volt_Ctrl
- Volt_Zone01
- Vcmd_GroupMin1
- TV_PressCmd
- O2An_CtrlMode
- Step_TempOffset01

Only Temp_MeanUpper currently shows group: Peak Temperature Limits.

## Conclusion
The recipe steps/params exist, but STEP grouping is mostly not applied in current data. The group seed for STEP aliases is not fully reflected in API output.
