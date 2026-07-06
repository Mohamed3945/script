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
public class ConfigurationDefinitionDto {
    private Long id;
    private String code;
    private String name;
    private ConfigurationValueType valueType;
    private String unit;
    private String questionForForm;
    private String questionGroup;
    private Integer displayOrder;
    private boolean active;
}
