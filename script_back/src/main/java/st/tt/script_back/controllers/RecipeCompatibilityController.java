package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeCompatibilityResultDto;
import st.tt.script_back.dto.RecipeCompatibleMachineSearchRequestDto;
import st.tt.script_back.services.RecipeCompatibilityService;

/**
 * Expose les recherches de compatibilité entre une recette et les équipements disponibles.
 *
 * <p>La décision de compatibilité est déléguée au service métier afin de conserver dans
 * un seul endroit les règles relatives aux chambres, capacités et configurations requises.</p>
 */
@RestController
@RequestMapping("/api/recipe-compatibility")
public class RecipeCompatibilityController {

    private final RecipeCompatibilityService recipeCompatibilityService;

    /**
     * Crée le contrôleur de compatibilité.
     *
     * @param recipeCompatibilityService service appliquant les règles de compatibilité
     */
    public RecipeCompatibilityController(RecipeCompatibilityService recipeCompatibilityService) {
        this.recipeCompatibilityService = recipeCompatibilityService;
    }

    /**
     * Recherche les machines compatibles avec les critères fournis.
     *
     * @param request critères de recherche et recette à analyser
     * @return résultat contenant les machines compatibles et les informations de compatibilité
     */
    @PostMapping("/compatible-machines")
    public RecipeCompatibilityResultDto findCompatibleMachines(
            @RequestBody RecipeCompatibleMachineSearchRequestDto request) {
        return recipeCompatibilityService.findCompatibleMachines(request);
    }
}
