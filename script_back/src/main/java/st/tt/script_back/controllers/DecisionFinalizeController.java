package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.DecisionFinalizeRequestDto;
import st.tt.script_back.dto.DecisionFinalizeResponseDto;
import st.tt.script_back.services.DecisionFinalizeService;

/** Finalise une exécution de décision et déclenche la création d'une recette dérivée. */
@RestController
@RequestMapping("/api/decision-executions")
public class DecisionFinalizeController {

    private final DecisionFinalizeService decisionFinalizeService;

    /** @param decisionFinalizeService service de finalisation du parcours */
    public DecisionFinalizeController(DecisionFinalizeService decisionFinalizeService) {
        this.decisionFinalizeService = decisionFinalizeService;
    }

    /**
     * Enregistre les réponses finales et crée la recette dérivée correspondante.
     * @param request réponses et profil de résultat de l'exécution
     * @return résultat de finalisation, incluant la recette créée
     */
    @PostMapping("/finalize")
    public DecisionFinalizeResponseDto finalizeDecisionAndCreateDerivedRecipe(
            @RequestBody DecisionFinalizeRequestDto request) {
        return decisionFinalizeService.finalizeDecisionAndCreateDerivedRecipe(request);
    }
}
