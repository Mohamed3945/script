package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import st.tt.script_back.services.DecisionTransitionService;
import st.tt.script_back.dto.DecisionNextRequestDto;
import st.tt.script_back.dto.DecisionNextResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Calcule la prochaine question du parcours selon les réponses déjà fournies. */
@RestController
@RequestMapping("/api/transitions")
public class DecisionTransitionController {
    private final DecisionTransitionService decisionTransitionService;

    /** @param decisionTransitionService service d'évaluation des transitions */
    public DecisionTransitionController(DecisionTransitionService decisionTransitionService) {
        this.decisionTransitionService = decisionTransitionService;
    }

    /**
     * Détermine la prochaine étape du questionnaire.
     * @param request question courante et réponse sélectionnée
     * @return prochaine question, ou indication de fin du parcours
     */
    @PostMapping("/next")
    public DecisionNextResponseDto getNextTransition(@RequestBody DecisionNextRequestDto request) {
        return decisionTransitionService.getNextTransition(request);
    }

}
