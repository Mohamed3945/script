package st.tt.script_back.mappers;

import st.tt.script_back.dto.DecisionResultProfileDto;
import st.tt.script_back.entities.DecisionResultProfile;
import org.springframework.stereotype.Component;

/**
 * DecisionResultProfileMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class DecisionResultProfileMapper {
    /**
     * Executes toResultProfileDto.
     *
     * @param profile input argument consumed by toResultProfileDto.
     * @return computed DecisionResultProfileDto result returned by toResultProfileDto.
     */
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
