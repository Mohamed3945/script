package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepKind;

/**
 * Detail d'un SP non modifie alors qu'il etait modifiable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeUntouchedModifiableItemDto {
    private Long stepId;
    private String stepCode;
    private String stepName;
    private StepKind stepKind;
    private Integer stepOrderIndex;

    private Long definitionId;
    private String parameterName;
    private String parameterGroup;
    private Integer parameterGroupOrder;
    private ParameterValueType valueType;

    private String currentDisplayValue;
}
