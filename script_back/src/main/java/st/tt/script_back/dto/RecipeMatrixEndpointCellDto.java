package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatrixEndpointCellDto {
    private Long stepId;
    private Long endpointId;
    private String clause;
    private Integer conditionCount;
    private String summaryLabel;
    private boolean lockedByGolden;
}