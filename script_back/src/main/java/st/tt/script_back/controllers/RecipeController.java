package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.services.RecipeQueryService;

/**
 * RecipeController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeQueryService recipeQueryService;

    /**
     * Executes RecipeController.
     *
     * @param recipeQueryService input argument consumed by RecipeController.
     */
    public RecipeController(RecipeQueryService recipeQueryService) {
        this.recipeQueryService = recipeQueryService;
    }

    /**
     * Executes getRecipes.
     *
         * @param recipeKind input argument consumed by getRecipes.
         * @param golden input argument consumed by getRecipes.
     * @return computed List<RecipeDto> result returned by getRecipes.
     */
    @GetMapping("/recipes")
    public List<RecipeDto> getRecipes(
            @RequestParam(required = false) RecipeKind recipeKind,
            @RequestParam(required = false) Boolean golden) {
        return recipeQueryService.getRecipes(recipeKind, golden);
    }

    /**
     * Executes getRecipe.
     *
     * @param id input argument consumed by getRecipe.
     * @return computed RecipeDto result returned by getRecipe.
     */
    @GetMapping("/recipes/{id}")
    public RecipeDto getRecipe(@PathVariable Long id) {
        return recipeQueryService.getRecipe(id);
    }

    /**
     * Executes getRecipeSteps.
     *
     * @param recipeId input argument consumed by getRecipeSteps.
         * @param stepKind input argument consumed by getRecipeSteps.
     * @return computed List<StepDto> result returned by getRecipeSteps.
     */
    @GetMapping("/recipes/{recipeId}/steps")
    public List<StepDto> getRecipeSteps(
            @PathVariable Long recipeId,
            @RequestParam(required = false) StepKind stepKind) {
        return recipeQueryService.getRecipeSteps(recipeId, stepKind);
    }

    /**
     * Executes getStep.
     *
     * @param id input argument consumed by getStep.
     * @return computed StepDto result returned by getStep.
     */
    @GetMapping("/steps/{id}")
    public StepDto getStep(@PathVariable Long id) {
        return recipeQueryService.getStep(id);
    }

    /**
     * Executes getStepParameters.
     *
     * @param stepId input argument consumed by getStepParameters.
     * @return computed List<StepParameterDto> result returned by getStepParameters.
     */
    @GetMapping("/steps/{stepId}/parameters")
    public List<StepParameterDto> getStepParameters(@PathVariable Long stepId) {
        return recipeQueryService.getStepParameters(stepId);
    }

    /**
     * Executes getStepParameter.
     *
     * @param id input argument consumed by getStepParameter.
     * @return computed StepParameterDto result returned by getStepParameter.
     */
    @GetMapping("/step-parameters/{id}")
    public StepParameterDto getStepParameter(@PathVariable Long id) {
        return recipeQueryService.getStepParameter(id);
    }

    /**
     * Executes getRecipeStepParameterGrid.
     *
     * @param recipeId input argument consumed by getRecipeStepParameterGrid.
     * @return computed List<StepParameterGridRowDto> result returned by getRecipeStepParameterGrid.
     */
    @GetMapping("/recipes/{recipeId}/step-parameter-grid")
    public List<StepParameterGridRowDto> getRecipeStepParameterGrid(@PathVariable Long recipeId) {
        return recipeQueryService.getRecipeStepParameterGrid(recipeId);
    }

    /**
     * Executes getRecipeMatrix.
     *
     * @param recipeId input argument consumed by getRecipeMatrix.
     * @return computed RecipeMatrixDto result returned by getRecipeMatrix.
     */
    @GetMapping("/recipes/{recipeId}/matrix")
    public RecipeMatrixDto getRecipeMatrix(@PathVariable Long recipeId) {
        return recipeQueryService.getRecipeMatrix(recipeId);
    }
}
