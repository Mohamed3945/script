package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ConfigurationValueType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequiredConfigurationDto {
    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private String configurationDefinitionName;
    private ConfigurationValueType valueType;
    private String unit;
    private String questionForForm;
    private String questionGroup;
    private Integer displayOrder;
}
