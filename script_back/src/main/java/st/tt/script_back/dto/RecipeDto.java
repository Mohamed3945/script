package st.tt.script_back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RecipeIapcMode;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeResumableMode;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.enums.RecipeWaferMode;

/**
 * RecipeDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private Long id;
    private RecipeKind recipeKind;
    private Long parentRecipeId;
    private String name;
    private String description;
    private Long creatorId;
    private Long revisorId;
    private String processFamily;
    private RecipeStatus status;
    private Integer version;
    private boolean frozen;
    private RecipeWaferMode wafer;
    @JsonProperty("iAPC")
    private RecipeIapcMode iapc;
    private RecipeResumableMode resumable;
    private String chamberType;
    private String accessDisplayGroups;
    private String accessModifyGroups;
    private String udaFile;
    private String type;
    private Integer maxTime;
    private String template;
}
