package st.tt.script_back.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeConfigurationConstraintDto {
    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private BigDecimal requestedValue;
}
