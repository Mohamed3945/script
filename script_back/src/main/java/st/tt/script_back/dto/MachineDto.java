package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.PlatformType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineDto {
    private Long id;
    private String code;
    private String name;
    private PlatformType platformType;
}
