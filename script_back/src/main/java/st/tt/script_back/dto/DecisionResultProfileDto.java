package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DecisionResultProfileDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResultProfileDto {
    private Long id;
    private String code;
    private String description;
    private boolean active;
    private Long goldenRecipeId;
    private Long machineId;
    
}
