package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.services.RecipeQueryService;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeQueryService recipeQueryService;

    public RecipeController(RecipeQueryService recipeQueryService) {
        this.recipeQueryService = recipeQueryService;
    }

    @GetMapping("/recipes")
    public List<RecipeDto> getRecipes(
            @RequestParam(required = false) RecipeKind recipeKind,
            @RequestParam(required = false) Boolean golden) {
        return recipeQueryService.getRecipes(recipeKind, golden);
    }

    @GetMapping("/recipes/{id}")
    public RecipeDto getRecipe(@PathVariable Long id) {
        return recipeQueryService.getRecipe(id);
    }

    @GetMapping("/recipes/{recipeId}/steps")
    public List<StepDto> getRecipeSteps(
            @PathVariable Long recipeId,
            @RequestParam(required = false) StepKind stepKind) {
        return recipeQueryService.getRecipeSteps(recipeId, stepKind);
    }

    @GetMapping("/steps/{id}")
    public StepDto getStep(@PathVariable Long id) {
        return recipeQueryService.getStep(id);
    }

    @GetMapping("/steps/{stepId}/parameters")
    public List<StepParameterDto> getStepParameters(@PathVariable Long stepId) {
        return recipeQueryService.getStepParameters(stepId);
    }

    @GetMapping("/step-parameters/{id}")
    public StepParameterDto getStepParameter(@PathVariable Long id) {
        return recipeQueryService.getStepParameter(id);
    }

    @GetMapping("/recipes/{recipeId}/step-parameter-grid")
    public List<StepParameterGridRowDto> getRecipeStepParameterGrid(@PathVariable Long recipeId) {
        return recipeQueryService.getRecipeStepParameterGrid(recipeId);
    }
}
