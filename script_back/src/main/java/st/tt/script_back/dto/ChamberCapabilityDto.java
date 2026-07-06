package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.CapabilityCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChamberCapabilityDto {
    private Long id;
    private String code;
    private String label;
    private CapabilityCategory category;
    private boolean active;
}
