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
import st.tt.script_back.services.RecipeService;
import st.tt.script_back.services.StepParameterService;
import st.tt.script_back.services.StepService;

/**
 * RecipeCommandController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api")
public class RecipeCommandController {

    private final RecipeService recipeService;
    private final StepService stepService;
    private final StepParameterService stepParameterService;

    /**
     * Executes RecipeCommandController.
     *
     * @param recipeService input argument consumed by RecipeCommandController.
     * @param stepService input argument consumed by RecipeCommandController.
     * @param stepParameterService input argument consumed by RecipeCommandController.
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
     * Executes createRecipe.
     *
     * @param request input argument consumed by createRecipe.
         * @param resultProfileId input argument consumed by createRecipe.
     * @return computed RecipeDto result returned by createRecipe.
     */
    @PostMapping("/recipes")
    public RecipeDto createRecipe(
            @RequestBody RecipeDto request,
            @RequestParam(required = false) Long resultProfileId) {
        return recipeService.createRecipe(request, resultProfileId);
    }

    /**
     * Executes updateRecipe.
     *
     * @param recipeId input argument consumed by updateRecipe.
     * @param request input argument consumed by updateRecipe.
     * @return computed RecipeDto result returned by updateRecipe.
     */
    @PutMapping("/recipes/{recipeId}")
    public RecipeDto updateRecipe(
            @PathVariable Long recipeId,
            @RequestBody RecipeDto request) {
        return recipeService.updateRecipe(recipeId, request);
    }

    /**
     * Executes deleteRecipe.
     *
     * @param recipeId input argument consumed by deleteRecipe.
     */
    @DeleteMapping("/recipes/{recipeId}")
    public void deleteRecipe(@PathVariable Long recipeId) {
        recipeService.deleteRecipe(recipeId);
    }

    /**
     * Executes createStep.
     *
     * @param recipeId input argument consumed by createStep.
     * @param request input argument consumed by createStep.
     * @return computed StepDto result returned by createStep.
     */
    @PostMapping("/recipes/{recipeId}/steps")
    public StepDto createStep(
            @PathVariable Long recipeId,
            @RequestBody StepDto request) {
        return stepService.createStep(recipeId, request);
    }

    /**
     * Executes updateStep.
     *
     * @param stepId input argument consumed by updateStep.
     * @param request input argument consumed by updateStep.
     * @return computed StepDto result returned by updateStep.
     */
    @PutMapping("/steps/{stepId}")
    public StepDto updateStep(
            @PathVariable Long stepId,
            @RequestBody StepDto request) {
        return stepService.updateStep(stepId, request);
    }

    /**
     * Executes deleteStep.
     *
     * @param stepId input argument consumed by deleteStep.
     */
    @DeleteMapping("/steps/{stepId}")
    public void deleteStep(@PathVariable Long stepId) {
        stepService.deleteStep(stepId);
    }

    /**
     * Executes createStepParameter.
     *
     * @param stepId input argument consumed by createStepParameter.
     * @param request input argument consumed by createStepParameter.
     * @return computed StepParameterDto result returned by createStepParameter.
     */
    @PostMapping("/steps/{stepId}/parameters")
    public StepParameterDto createStepParameter(
            @PathVariable Long stepId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.createStepParameter(stepId, request);
    }

    /**
     * Executes updateStepParameter.
     *
     * @param stepParameterId input argument consumed by updateStepParameter.
     * @param request input argument consumed by updateStepParameter.
     * @return computed StepParameterDto result returned by updateStepParameter.
     */
    @PutMapping("/step-parameters/{stepParameterId}")
    public StepParameterDto updateStepParameter(
            @PathVariable Long stepParameterId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.updateStepParameter(stepParameterId, request);
    }

    /**
     * Executes deleteStepParameter.
     *
     * @param stepParameterId input argument consumed by deleteStepParameter.
     */
    @DeleteMapping("/step-parameters/{stepParameterId}")
    public void deleteStepParameter(@PathVariable Long stepParameterId) {
        stepParameterService.deleteStepParameter(stepParameterId);
    }
}
