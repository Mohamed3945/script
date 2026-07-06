package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.PlatformType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompatibleMachineDto {
    private Long machineId;
    private String machineCode;
    private String machineName;
    private PlatformType platformType;
    private List<CompatibleChamberDto> compatibleChambers;
}
