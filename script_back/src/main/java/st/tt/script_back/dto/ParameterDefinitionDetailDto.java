package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.enums.XmlSection;

/**
 * ParameterDefinitionDetailDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
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
    private ParameterScope stepType;
    private XmlSection xmlSection;
    private String defaultValueJson;
    private String parameterGroup;
    private Integer parameterGroupOrder;
    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private String configurationDefinitionName;
    private List<ParameterOptionDto> options;
}


