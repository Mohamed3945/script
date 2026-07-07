# STEP enum-like options extracted from XML

Source: `GOLDEN V5 Rad ISSG Non Critique OL Manuel 5 Torr.xml`

## Categorical aliases

- `Lift_PinPos`: `Lowered` | `Raised`
- `MagLev_SpeedCmd`: `Rotate Use Step` | `Stop`
- `O2An_CtrlMode`: `Disable`
- `SelOxideDest`: `SlitValve`
- `StepPressOffsetApply`: `No`
- `Temp_IncTemp1`: `No`
- `Temp_IncTemp2`: `No`
- `Temp_IncTemp3`: `No`
- `Temp_IncTemp4`: `No`
- `Temp_IncTemp5`: `No`
- `Temp_IncTemp6`: `No`
- `Temp_IncTemp7`: `No`
- `Temp_IncTemp8`: `No`
- `Temp_Mode`: `Const Temp` | `Const Voltage` | `Power` | `Ramp Temp` | `StabTemp`
- `TV_PressCmd`: `CtrlPress` | `NoChange` | `Servo to Pressure`
- `Vcmd_RangeCheck`: `Disable`
- `Vcmd_RangeCheckUsage`: `Use all Vcmd's` | `useAllVcmds`
- `Volt_Ctrl`: `Use Vltg Target` | `UseTarget` | `UseValues`

## Notes

- `OffsetTable` appears in the XML but only carries empty values here, so it is not treated as an enum option list.
- The numeric aliases like `Temp_Target`, `Volt_Zone01`, `N2_Flow_Set`, etc. are not listed here because they are parameter values, not option choices.
