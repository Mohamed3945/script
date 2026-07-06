package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChamberDto {
    private Long id;
    private Long machineId;
    private String machineCode;
    private String machineName;
    private String code;
    private String name;
}
