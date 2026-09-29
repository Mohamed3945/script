package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.DuplicateGoldenRecipeRequestDto;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.services.RecipeService;
import st.tt.script_back.services.StepParameterService;
import st.tt.script_back.services.StepService;
import java.util.List;

/** Expose les commandes de création, modification et suppression des recettes et étapes. */
@RestController
@RequestMapping("/api")
public class RecipeCommandController {

    private final RecipeService recipeService;
    private final StepService stepService;
    private final StepParameterService stepParameterService;

    /**
     * @param recipeService service de modification des recettes
     * @param stepService service de modification des étapes
     * @param stepParameterService service de modification des paramètres d'étape
     */
    public RecipeCommandController(
            RecipeService recipeService,
            StepService stepService,
            StepParameterService stepParameterService) {
        this.recipeService = recipeService;
        this.stepService = stepService;
        this.stepParameterService = stepParameterService;
    }

    /**
     * Crée une recette, éventuellement à partir d'un profil de résultat.
     * @param request données de la recette
     * @param resultProfileId profil de résultat facultatif
     * @return recette créée
     */
    @PostMapping("/recipes")
    public RecipeDto createRecipe(
            @RequestBody RecipeDto request,
            @RequestParam(required = false) Long resultProfileId) {
        return recipeService.createRecipe(request, resultProfileId);
    }

    /**
     * Duplique une recette golden pour créer une recette de travail dérivée.
     * @param request recette source et informations de duplication
     * @return recette dérivée créée
     */
    @PostMapping("/recipes/duplicate-golden")
    public RecipeDto duplicateGoldenRecipe(
            @RequestBody DuplicateGoldenRecipeRequestDto request) {
        return recipeService.duplicateGoldenRecipe(request);
    }

    /** @param recipeId recette à modifier @param request nouvelles données @return recette mise à jour */
    @PutMapping("/recipes/{recipeId}")
    public RecipeDto updateRecipe(
            @PathVariable Long recipeId,
            @RequestBody RecipeDto request) {
        return recipeService.updateRecipe(recipeId, request);
    }

    /** @param recipeId identifiant de la recette à supprimer */
    @DeleteMapping("/recipes/{recipeId}")
    public void deleteRecipe(@PathVariable Long recipeId) {
        recipeService.deleteRecipe(recipeId);
    }

    /** @param recipeId recette parente @param request données de l'étape @return étape créée */
    @PostMapping("/recipes/{recipeId}/steps")
    public StepDto createStep(
            @PathVariable Long recipeId,
            @RequestBody StepDto request) {
        return stepService.createStep(recipeId, request);
    }

    /**
     * Crée un même paramètre dans toutes les étapes correspondant au type demandé.
     * @param recipeId recette dont les étapes sont ciblées
     * @param stepKind type d'étape à traiter
     * @param request définition et valeur initiale du paramètre
     * @return paramètres créés dans les étapes concernées
     */
    @PostMapping("/recipes/{recipeId}/step-parameters/propagate")
    public List<StepParameterDto> propagateStepParameterToRecipeSteps(
            @PathVariable Long recipeId,
            @RequestParam StepKind stepKind,
            @RequestBody StepParameterDto request) {
        return stepParameterService.createRecipeWideStepParameter(recipeId, stepKind, request);
    }
    /** @param stepId étape à modifier @param request nouvelles données @return étape mise à jour */
    @PutMapping("/steps/{stepId}")
    public StepDto updateStep(
            @PathVariable Long stepId,
            @RequestBody StepDto request) {
        return stepService.updateStep(stepId, request);
    }

    /** @param stepId identifiant de l'étape à supprimer */
    @DeleteMapping("/steps/{stepId}")
    public void deleteStep(@PathVariable Long stepId) {
        stepService.deleteStep(stepId);
    }

    /** @param stepId étape cible @param request paramètre à créer @return paramètre créé */
    @PostMapping("/steps/{stepId}/parameters")
    public StepParameterDto createStepParameter(
            @PathVariable Long stepId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.createStepParameter(stepId, request);
    }

    /** @param stepId étape cible @param requests paramètres à créer @return paramètres créés */
    @PostMapping("/steps/{stepId}/parameters/bulk")
    public List<StepParameterDto> createStepParametersBulk(
            @PathVariable Long stepId,
            @RequestBody List<StepParameterDto> requests) {
        return stepParameterService.createStepParametersBulk(stepId, requests);
    }

    /** @param stepParameterId paramètre à modifier @param request nouvelles données @return paramètre mis à jour */
    @PutMapping("/step-parameters/{stepParameterId}")
    public StepParameterDto updateStepParameter(
            @PathVariable Long stepParameterId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.updateStepParameter(stepParameterId, request);
    }

    /** @param stepParameterId identifiant du paramètre à supprimer */
    @DeleteMapping("/step-parameters/{stepParameterId}")
    public void deleteStepParameter(@PathVariable Long stepParameterId) {
        stepParameterService.deleteStepParameter(stepParameterId);
    }

    /**
     * Supprime une définition de paramètre de toutes les étapes de la recette.
     * @param recipeId recette concernée
     * @param definitionId définition de paramètre à retirer
     */
    @DeleteMapping("/recipes/{recipeId}/parameter-definitions/{definitionId}")
    public void deleteRecipeWideStepParameter(
            @PathVariable Long recipeId,
            @PathVariable Long definitionId) {
        stepParameterService.deleteRecipeWideStepParameter(recipeId, definitionId);
    }
}
