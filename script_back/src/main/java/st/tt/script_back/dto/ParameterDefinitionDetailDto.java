package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinitionDetailDto {
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
    private List<ParameterOptionDto> options;
}
