package st.tt.script_back.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionOptionAdminRequestDto {
    private String label;
    private String value;
    private int orderIndex;
}