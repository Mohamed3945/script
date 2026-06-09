package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResultProfileDto {
    private Long id;
    private String code;
    private String description;
    private Long goldenRecipeId;
    private Long machineId;
    
}
