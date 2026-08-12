package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateGoldenRecipeRequestDto {
    private Long sourceGoldenRecipeId;
    private String targetName;
    private Long creatorId;
    private boolean includeFormulas;
    private boolean includeRequiredCapabilities;
    private boolean includeRequiredConfigurations;
}