package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.DecisionFinalizeRequestDto;
import st.tt.script_back.dto.DecisionFinalizeResponseDto;
import st.tt.script_back.services.DecisionFinalizeService;

@RestController
@RequestMapping("/api/decision-executions")
public class DecisionFinalizeController {

    private final DecisionFinalizeService decisionFinalizeService;

    public DecisionFinalizeController(DecisionFinalizeService decisionFinalizeService) {
        this.decisionFinalizeService = decisionFinalizeService;
    }

    @PostMapping("/finalize")
    public DecisionFinalizeResponseDto finalizeDecisionAndCreateDerivedRecipe(
            @RequestBody DecisionFinalizeRequestDto request) {
        return decisionFinalizeService.finalizeDecisionAndCreateDerivedRecipe(request);
    }
}
