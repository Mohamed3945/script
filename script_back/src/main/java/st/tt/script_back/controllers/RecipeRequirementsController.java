package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeRequirementsDto;
import st.tt.script_back.services.RecipeRequirementsService;

@RestController
@RequestMapping("/api/recipes")
public class RecipeRequirementsController {

    private final RecipeRequirementsService recipeRequirementsService;

    public RecipeRequirementsController(RecipeRequirementsService recipeRequirementsService) {
        this.recipeRequirementsService = recipeRequirementsService;
    }

    @GetMapping("/{recipeId}/requirements")
    public RecipeRequirementsDto getRequirements(@PathVariable Long recipeId) {
        return recipeRequirementsService.getRecipeRequirements(recipeId);
    }

    @PostMapping("/{recipeId}/required-capabilities/{capabilityId}")
    public RecipeRequirementsDto addRequiredCapability(
            @PathVariable Long recipeId,
            @PathVariable Long capabilityId) {
        return recipeRequirementsService.addCapabilityToRecipe(recipeId, capabilityId);
    }

    @DeleteMapping("/{recipeId}/required-capabilities/{capabilityId}")
    public RecipeRequirementsDto removeRequiredCapability(
            @PathVariable Long recipeId,
            @PathVariable Long capabilityId) {
        return recipeRequirementsService.removeCapabilityFromRecipe(recipeId, capabilityId);
    }

    @PostMapping("/{recipeId}/required-configurations/{configurationDefinitionId}")
    public RecipeRequirementsDto addRequiredConfiguration(
            @PathVariable Long recipeId,
            @PathVariable Long configurationDefinitionId) {
        return recipeRequirementsService.addConfigurationDefinitionToRecipe(recipeId, configurationDefinitionId);
    }

    @DeleteMapping("/{recipeId}/required-configurations/{configurationDefinitionId}")
    public RecipeRequirementsDto removeRequiredConfiguration(
            @PathVariable Long recipeId,
            @PathVariable Long configurationDefinitionId) {
        return recipeRequirementsService.removeConfigurationDefinitionFromRecipe(recipeId, configurationDefinitionId);
    }
}
