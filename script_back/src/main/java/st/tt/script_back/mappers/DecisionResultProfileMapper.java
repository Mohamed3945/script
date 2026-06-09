package st.tt.script_back.mappers;

import st.tt.script_back.dto.DecisionResultProfileDto;
import st.tt.script_back.entities.DecisionResultProfile;
import org.springframework.stereotype.Component;

@Component
public class DecisionResultProfileMapper {
    public DecisionResultProfileDto toResultProfileDto(DecisionResultProfile profile) {
        if (profile == null) {
            return null;
        }
        return new DecisionResultProfileDto(
                profile.getId(),
                profile.getCode(),
                profile.getDescription(),
                profile.getGoldenRecipeId() ,
                profile.getMachineId() 
        );
    }
    
}
