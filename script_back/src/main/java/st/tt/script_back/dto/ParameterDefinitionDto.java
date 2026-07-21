package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.ParameterScope;

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
    private ParameterScope stepType;
    private String defaultValueJson;

    private Long parameterGroupId;
    private String parameterGroupName;
    private Integer parameterGroupOrder;
    private Integer orderIndexInGroup;

    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private String configurationDefinitionName;
}

