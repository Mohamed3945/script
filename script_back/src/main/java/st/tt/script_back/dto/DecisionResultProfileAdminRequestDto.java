package st.tt.script_back.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionResultProfileAdminRequestDto {
    private String code;
    private String description;
    private boolean active;
    private Long goldenRecipeId;
}