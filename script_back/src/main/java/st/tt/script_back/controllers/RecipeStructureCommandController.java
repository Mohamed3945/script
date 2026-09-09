package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.tt.script_back.dto.CreateStructuredStepRequestDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.services.RecipeStructureCommandService;

@RestController
@RequestMapping("/api")
public class RecipeStructureCommandController {

    private final RecipeStructureCommandService recipeStructureCommandService;

    public RecipeStructureCommandController(RecipeStructureCommandService recipeStructureCommandService) {
        this.recipeStructureCommandService = recipeStructureCommandService;
    }

    @PostMapping("/recipes/{recipeId}/steps/structured")
    public StepDto createStructuredStep(
            @PathVariable Long recipeId,
            @RequestBody CreateStructuredStepRequestDto request) {
        return recipeStructureCommandService.createStructuredStep(recipeId, request);
    }
}