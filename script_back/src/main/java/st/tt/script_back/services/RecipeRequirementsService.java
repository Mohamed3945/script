package st.tt.script_back.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.RecipeRequirementsDto;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.entities.ConfigurationDefinition;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.mappers.RecipeRequirementsMapper;
import st.tt.script_back.repositories.ChamberCapabilityRepository;
import st.tt.script_back.repositories.ConfigurationDefinitionRepository;
import st.tt.script_back.repositories.RecipeRepository;

@Service
public class RecipeRequirementsService {

    private final RecipeRepository recipeRepository;
    private final ChamberCapabilityRepository chamberCapabilityRepository;
    private final ConfigurationDefinitionRepository configurationDefinitionRepository;
    private final RecipeRequirementsMapper recipeRequirementsMapper;

    public RecipeRequirementsService(
            RecipeRepository recipeRepository,
            ChamberCapabilityRepository chamberCapabilityRepository,
            ConfigurationDefinitionRepository configurationDefinitionRepository,
            RecipeRequirementsMapper recipeRequirementsMapper) {
        this.recipeRepository = recipeRepository;
        this.chamberCapabilityRepository = chamberCapabilityRepository;
        this.configurationDefinitionRepository = configurationDefinitionRepository;
        this.recipeRequirementsMapper = recipeRequirementsMapper;
    }

    @Transactional(readOnly = true)
    public RecipeRequirementsDto getRecipeRequirements(Long recipeId) {
        Recipe recipe = recipeRepository.findByIdWithRequirements(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        return recipeRequirementsMapper.toDto(recipe);
    }

    @Transactional
    public RecipeRequirementsDto addCapabilityToRecipe(Long recipeId, Long capabilityId) {
        Recipe recipe = recipeRepository.findByIdWithRequirements(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        ChamberCapability capability = chamberCapabilityRepository.findById(capabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Capability with id " + capabilityId + " not found"));

        recipe.getRequiredCapabilities().add(capability);
        Recipe saved = recipeRepository.save(recipe);
        return recipeRequirementsMapper.toDto(saved);
    }

    @Transactional
    public RecipeRequirementsDto removeCapabilityFromRecipe(Long recipeId, Long capabilityId) {
        Recipe recipe = recipeRepository.findByIdWithRequirements(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        recipe.getRequiredCapabilities().removeIf(capability -> capability.getId().equals(capabilityId));
        Recipe saved = recipeRepository.save(recipe);
        return recipeRequirementsMapper.toDto(saved);
    }

    @Transactional
    public RecipeRequirementsDto addConfigurationDefinitionToRecipe(Long recipeId, Long configurationDefinitionId) {
        Recipe recipe = recipeRepository.findByIdWithRequirements(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        ConfigurationDefinition definition = configurationDefinitionRepository.findById(configurationDefinitionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ConfigurationDefinition with id " + configurationDefinitionId + " not found"));

        recipe.getRequiredConfigurationDefinitions().add(definition);
        Recipe saved = recipeRepository.save(recipe);
        return recipeRequirementsMapper.toDto(saved);
    }

    @Transactional
    public RecipeRequirementsDto removeConfigurationDefinitionFromRecipe(Long recipeId, Long configurationDefinitionId) {
        Recipe recipe = recipeRepository.findByIdWithRequirements(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        recipe.getRequiredConfigurationDefinitions()
                .removeIf(definition -> definition.getId().equals(configurationDefinitionId));
        Recipe saved = recipeRepository.save(recipe);
        return recipeRequirementsMapper.toDto(saved);
    }
}
