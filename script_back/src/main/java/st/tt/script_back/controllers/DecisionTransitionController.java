package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import st.tt.script_back.services.DecisionTransitionService;
import st.tt.script_back.dto.DecisionNextRequestDto;
import st.tt.script_back.dto.DecisionNextResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * DecisionTransitionController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api/transitions")
public class DecisionTransitionController {
    private final DecisionTransitionService decisionTransitionService;

    /**
     * Executes DecisionTransitionController.
     *
     * @param decisionTransitionService input argument consumed by DecisionTransitionController.
     */
    public DecisionTransitionController(DecisionTransitionService decisionTransitionService) {
        this.decisionTransitionService = decisionTransitionService;
    }

    /**
     * Executes getNextTransition.
     *
     * @param request input argument consumed by getNextTransition.
     * @return computed DecisionNextResponseDto result returned by getNextTransition.
     */
    @PostMapping("/next")
    public DecisionNextResponseDto getNextTransition(@RequestBody DecisionNextRequestDto request) {
        return decisionTransitionService.getNextTransition(request);
    }

}
