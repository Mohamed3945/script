package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeCustomizationSummaryDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.services.RecipeCustomizationSummaryService;
import st.tt.script_back.services.RecipeQueryService;

/**
 * Expose les opérations de consultation des recettes et de leur structure.
 *
 * <p>Les réponses sont destinées aux écrans de consultation et d'édition :
 * recettes, étapes, paramètres, grille de paramètres et matrice de recette.
 * Les règles d'accès et l'assemblage des projections restent délégués aux
 * services métier.
 */
@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeQueryService recipeQueryService;
    private final RecipeCustomizationSummaryService recipeCustomizationSummaryService;

    /**
     * Crée le contrôleur avec ses services de lecture.
     *
     * @param recipeQueryService service de consultation des recettes et étapes
     * @param recipeCustomizationSummaryService service de synthèse des personnalisations
     */
    public RecipeController(
            RecipeQueryService recipeQueryService,
            RecipeCustomizationSummaryService recipeCustomizationSummaryService) {
        this.recipeQueryService = recipeQueryService;
        this.recipeCustomizationSummaryService = recipeCustomizationSummaryService;
    }

    /**
     * Retourne les recettes visibles par l'utilisateur courant.
     *
     * <p>Un super-utilisateur peut filtrer par type ou par statut golden.
     * Un utilisateur standard ne reçoit que ses recettes dérivées.
     * Les résultats sont classés de la révision la plus récente à la plus ancienne.
     *
     * @param recipeKind filtre optionnel sur le type de recette
     * @param golden filtre optionnel : {@code true} pour les recettes golden,
     *        {@code false} pour les autres recettes
     * @return recettes accessibles sous forme de DTO
     */
    @GetMapping("/recipes")
    public List<RecipeDto> getRecipes(
            @RequestParam(required = false) RecipeKind recipeKind,
            @RequestParam(required = false) Boolean golden) {
        return recipeQueryService.getRecipes(recipeKind, golden);
    }

    /**
     * Retourne le détail d'une recette accessible.
     *
     * @param id identifiant de la recette
     * @return recette convertie en DTO
     * @throws jakarta.persistence.EntityNotFoundException si la recette n'existe pas
     * @throws org.springframework.security.access.AccessDeniedException si elle n'est pas accessible
     */
    @GetMapping("/recipes/{id}")
    public RecipeDto getRecipe(@PathVariable Long id) {
        return recipeQueryService.getRecipe(id);
    }

    /**
     * Retourne les étapes d'une recette dans leur ordre d'exécution.
     *
     * @param recipeId identifiant de la recette
     * @param stepKind filtre optionnel sur le type d'étape
     * @return étapes triées par {@code orderIndex}
     */
    @GetMapping("/recipes/{recipeId}/steps")
    public List<StepDto> getRecipeSteps(
            @PathVariable Long recipeId,
            @RequestParam(required = false) StepKind stepKind) {
        return recipeQueryService.getRecipeSteps(recipeId, stepKind);
    }

    /**
     * Retourne le détail d'une étape.
     *
     * @param id identifiant de l'étape
     * @return étape convertie en DTO
     * @throws jakarta.persistence.EntityNotFoundException si l'étape n'existe pas
     */
    @GetMapping("/steps/{id}")
    public StepDto getStep(@PathVariable Long id) {
        return recipeQueryService.getStep(id);
    }

    /**
     * Retourne les paramètres d'une étape avec leur définition et leur état calculé.
     *
     * @param stepId identifiant de l'étape
     * @return paramètres associés à l'étape
     */
    @GetMapping("/steps/{stepId}/parameters")
    public List<StepParameterDto> getStepParameters(@PathVariable Long stepId) {
        return recipeQueryService.getStepParameters(stepId);
    }

    /**
     * Retourne un paramètre d'étape et indique s'il est produit par une formule.
     *
     * @param id identifiant du paramètre d'étape
     * @return paramètre converti en DTO
     */
    @GetMapping("/step-parameters/{id}")
    public StepParameterDto getStepParameter(@PathVariable Long id) {
        return recipeQueryService.getStepParameter(id);
    }

    /**
     * Retourne les paramètres calculés qui dépendent directement ou indirectement
     * de la valeur du paramètre fourni.
     *
     * @param id identifiant du paramètre source
     * @return identifiants des paramètres calculés impactés
     */
    @GetMapping("/step-parameters/{id}/computed-dependents")
    public List<Long> getComputedDependents(@PathVariable Long id) {
        return recipeQueryService.getComputedDependentsByStepParameterId(id);
    }

    /**
     * Construit la grille des paramètres d'une recette pour l'interface d'édition.
     *
     * @param recipeId identifiant de la recette
     * @return lignes triées par groupe, position dans le groupe puis nom
     */
    @GetMapping("/recipes/{recipeId}/step-parameter-grid")
    public List<StepParameterGridRowDto> getRecipeStepParameterGrid(@PathVariable Long recipeId) {
        return recipeQueryService.getRecipeStepParameterGrid(recipeId);
    }

    /**
     * Construit la matrice croisant les étapes et les définitions de paramètres.
     *
     * @param recipeId identifiant de la recette
     * @return matrice comprenant colonnes, lignes et cellules de paramètres
     */
    @GetMapping("/recipes/{recipeId}/matrix")
    public RecipeMatrixDto getRecipeMatrix(@PathVariable Long recipeId) {
        return recipeQueryService.getRecipeMatrix(recipeId);
    }

    /**
     * Retourne le résumé de personnalisation d'une recette dérivée avant export XML.
     * @param recipeId identifiant de la recette
     * @param limit nombre maximal d'éléments détaillés à retourner
     * @return statistiques et éléments non personnalisés de la recette
     */
    @GetMapping("/recipes/{recipeId}/customization-summary")
    public RecipeCustomizationSummaryDto getCustomizationSummary(
            @PathVariable Long recipeId,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        return recipeCustomizationSummaryService.computeForRecipe(recipeId, limit);
    }
}
