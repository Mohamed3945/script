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

@RestController
@RequestMapping("/api")
public class RecipeCommandController {

    private final RecipeService recipeService;
    private final StepService stepService;
    private final StepParameterService stepParameterService;

    public RecipeCommandController(
            RecipeService recipeService,
            StepService stepService,
            StepParameterService stepParameterService) {
        this.recipeService = recipeService;
        this.stepService = stepService;
        this.stepParameterService = stepParameterService;
    }

    @PostMapping("/recipes")
    public RecipeDto createRecipe(
            @RequestBody RecipeDto request,
            @RequestParam(required = false) Long resultProfileId) {
        return recipeService.createRecipe(request, resultProfileId);
    }

    @PutMapping("/recipes/{recipeId}")
    public RecipeDto updateRecipe(
            @PathVariable Long recipeId,
            @RequestBody RecipeDto request) {
        return recipeService.updateRecipe(recipeId, request);
    }

    @DeleteMapping("/recipes/{recipeId}")
    public void deleteRecipe(@PathVariable Long recipeId) {
        recipeService.deleteRecipe(recipeId);
    }

    @PostMapping("/recipes/{recipeId}/steps")
    public StepDto createStep(
            @PathVariable Long recipeId,
            @RequestBody StepDto request) {
        return stepService.createStep(recipeId, request);
    }

    @PutMapping("/steps/{stepId}")
    public StepDto updateStep(
            @PathVariable Long stepId,
            @RequestBody StepDto request) {
        return stepService.updateStep(stepId, request);
    }

    @DeleteMapping("/steps/{stepId}")
    public void deleteStep(@PathVariable Long stepId) {
        stepService.deleteStep(stepId);
    }

    @PostMapping("/steps/{stepId}/parameters")
    public StepParameterDto createStepParameter(
            @PathVariable Long stepId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.createStepParameter(stepId, request);
    }

    @PutMapping("/step-parameters/{stepParameterId}")
    public StepParameterDto updateStepParameter(
            @PathVariable Long stepParameterId,
            @RequestBody StepParameterDto request) {
        return stepParameterService.updateStepParameter(stepParameterId, request);
    }

    @DeleteMapping("/step-parameters/{stepParameterId}")
    public void deleteStepParameter(@PathVariable Long stepParameterId) {
        stepParameterService.deleteStepParameter(stepParameterId);
    }
}
