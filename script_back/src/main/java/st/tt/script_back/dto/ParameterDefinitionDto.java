package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepType;

/**
 * ParameterDefinitionDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinitionDto {
    private Long id;
    private String code;
    private String name;
    private String alias;
    private String unit;
    private String description;
    private ParameterValueType valueType;
    private boolean requiredOnStep;
    private StepType stepType;
    private String defaultValueJson;
    private String parameterGroup;
    private Integer parameterGroupOrder;
    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private String configurationDefinitionName;
}
