package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepEndpointDto {

    private Long id;
    private Long stepId;

    // null  → <ENDPOINT />  (time-based, aucune condition)
    // "AND" → toutes les conditions doivent être satisfaites
    // "OR"  → au moins une condition doit être satisfaite
    private String clause;

    // true  → endpoint verrouillé depuis la golden
    // false → endpoint éditable
    private Boolean lockedByGolden;

    private List<StepEndpointConditionDto> conditions;
}