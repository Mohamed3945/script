package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RoundingPolicy;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComputationFormulaDto {
    private Long id;
    private Long recipeId;
    private String targetStepCode;
    private String targetDefinitionPath;
    private String expression;
    private RoundingPolicy roundingMode;
    private Integer decimals;
    private String label;
    private List<FormulaReferenceDto> references;
}