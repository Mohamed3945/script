package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeCompatibilityResultDto;
import st.tt.script_back.dto.RecipeCompatibleMachineSearchRequestDto;
import st.tt.script_back.services.RecipeCompatibilityService;

@RestController
@RequestMapping("/api/recipe-compatibility")
public class RecipeCompatibilityController {

    private final RecipeCompatibilityService recipeCompatibilityService;

    public RecipeCompatibilityController(RecipeCompatibilityService recipeCompatibilityService) {
        this.recipeCompatibilityService = recipeCompatibilityService;
    }

    @PostMapping("/compatible-machines")
    public RecipeCompatibilityResultDto findCompatibleMachines(
            @RequestBody RecipeCompatibleMachineSearchRequestDto request) {
        return recipeCompatibilityService.findCompatibleMachines(request);
    }
}
