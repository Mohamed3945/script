package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.CapabilityCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequiredCapabilityDto {
    private Long capabilityId;
    private String capabilityCode;
    private String capabilityLabel;
    private CapabilityCategory capabilityCategory;
}
