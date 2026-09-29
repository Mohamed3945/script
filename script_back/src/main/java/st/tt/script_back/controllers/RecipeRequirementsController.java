package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.RecipeRequirementsDto;
import st.tt.script_back.services.RecipeRequirementsService;

/** Gère les capacités et configurations requises par une recette. */
@RestController
@RequestMapping("/api/recipes")
public class RecipeRequirementsController {

    private final RecipeRequirementsService recipeRequirementsService;

    /** @param recipeRequirementsService service métier des exigences de recette */
    public RecipeRequirementsController(RecipeRequirementsService recipeRequirementsService) {
        this.recipeRequirementsService = recipeRequirementsService;
    }

    /** @param recipeId recette cible @return exigences actuellement associées */
    @GetMapping("/{recipeId}/requirements")
    public RecipeRequirementsDto getRequirements(@PathVariable Long recipeId) {
        return recipeRequirementsService.getRecipeRequirements(recipeId);
    }

    /** @param recipeId recette cible @param capabilityId capacité requise à ajouter @return exigences mises à jour */
    @PostMapping("/{recipeId}/required-capabilities/{capabilityId}")
    public RecipeRequirementsDto addRequiredCapability(
            @PathVariable Long recipeId,
            @PathVariable Long capabilityId) {
        return recipeRequirementsService.addCapabilityToRecipe(recipeId, capabilityId);
    }

    /** @param recipeId recette cible @param capabilityId capacité à retirer @return exigences mises à jour */
    @DeleteMapping("/{recipeId}/required-capabilities/{capabilityId}")
    public RecipeRequirementsDto removeRequiredCapability(
            @PathVariable Long recipeId,
            @PathVariable Long capabilityId) {
        return recipeRequirementsService.removeCapabilityFromRecipe(recipeId, capabilityId);
    }

    /** @param recipeId recette cible @param configurationDefinitionId configuration requise à ajouter @return exigences mises à jour */
    @PostMapping("/{recipeId}/required-configurations/{configurationDefinitionId}")
    public RecipeRequirementsDto addRequiredConfiguration(
            @PathVariable Long recipeId,
            @PathVariable Long configurationDefinitionId) {
        return recipeRequirementsService.addConfigurationDefinitionToRecipe(recipeId, configurationDefinitionId);
    }

    /** @param recipeId recette cible @param configurationDefinitionId configuration à retirer @return exigences mises à jour */
    @DeleteMapping("/{recipeId}/required-configurations/{configurationDefinitionId}")
    public RecipeRequirementsDto removeRequiredConfiguration(
            @PathVariable Long recipeId,
            @PathVariable Long configurationDefinitionId) {
        return recipeRequirementsService.removeConfigurationDefinitionFromRecipe(recipeId, configurationDefinitionId);
    }
}
