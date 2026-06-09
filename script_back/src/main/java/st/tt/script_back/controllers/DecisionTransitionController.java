package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import st.tt.script_back.services.DecisionTransitionService;
import st.tt.script_back.dto.DecisionNextRequestDto;
import st.tt.script_back.dto.DecisionNextResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/transitions")
public class DecisionTransitionController {
    private final DecisionTransitionService decisionTransitionService;

    public DecisionTransitionController(DecisionTransitionService decisionTransitionService) {
        this.decisionTransitionService = decisionTransitionService;
    }

    @PostMapping("/next")
    public DecisionNextResponseDto getNextTransition(@RequestBody DecisionNextRequestDto request) {
        return decisionTransitionService.getNextTransition(request);
    }

}
