package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.tt.script_back.dto.CreateStructuredStepRequestDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.services.RecipeStructureCommandService;

/**
 * Expose les commandes qui modifient la structure d'une recette.
 *
 * <p>Les contrôles d'autorisation et les règles de création sont appliqués par la
 * configuration Spring Security et par le service métier associé.</p>
 */
@RestController
@RequestMapping("/api")
public class RecipeStructureCommandController {

    private final RecipeStructureCommandService recipeStructureCommandService;

    /**
     * Crée le contrôleur de commandes de structure.
     *
     * @param recipeStructureCommandService service de modification de la structure des recettes
     */
    public RecipeStructureCommandController(RecipeStructureCommandService recipeStructureCommandService) {
        this.recipeStructureCommandService = recipeStructureCommandService;
    }

    /**
     * Ajoute une étape structurée à une recette.
     *
     * @param recipeId identifiant de la recette cible
     * @param request description de l'étape et de ses paramètres à créer
     * @return représentation de l'étape nouvellement créée
     */
    @PostMapping("/recipes/{recipeId}/steps/structured")
    public StepDto createStructuredStep(
            @PathVariable Long recipeId,
            @RequestBody CreateStructuredStepRequestDto request) {
        return recipeStructureCommandService.createStructuredStep(recipeId, request);
    }
}