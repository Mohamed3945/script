package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.entities.DecisionResultProfile;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.repositories.DecisionExecutionRepository;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.repositories.DecisionResultProfileRepository;
import st.tt.script_back.repositories.RecipeRepository;

/**
 * RecipeService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final DecisionResultProfileRepository decisionResultProfileRepository;
    private final DecisionExecutionRepository decisionExecutionRepository;
    private final RecipeMapper recipeMapper;

    /**
     * Executes RecipeService.
     *
     * @param recipeRepository input argument consumed by RecipeService.
     * @param decisionResultProfileRepository input argument consumed by RecipeService.
     * @param recipeMapper input argument consumed by RecipeService.
     */
    public RecipeService(
            RecipeRepository recipeRepository,
            DecisionResultProfileRepository decisionResultProfileRepository,
            DecisionExecutionRepository decisionExecutionRepository,
            RecipeMapper recipeMapper) {
        this.recipeRepository = recipeRepository;
        this.decisionResultProfileRepository = decisionResultProfileRepository;
        this.decisionExecutionRepository = decisionExecutionRepository;
        this.recipeMapper = recipeMapper;
    }

    /**
     * Executes createRecipe.
     *
     * @param request input argument consumed by createRecipe.
     * @param resultProfileId input argument consumed by createRecipe.
     * @return computed RecipeDto result returned by createRecipe.
     */
    @Transactional
    public RecipeDto createRecipe(RecipeDto request, Long resultProfileId) {
        if (request == null) {
            throw new IllegalArgumentException("Recipe payload is required");
        }

        RecipeKind kind = request.getRecipeKind();
        if (kind == null) {
            throw new IllegalArgumentException("recipeKind is required");
        }
        if (kind == RecipeKind.IMPORTED) {
            throw new IllegalArgumentException("Recipe creation supports only GOLDEN or DERIVED");
        }

        Recipe recipe = recipeMapper.toEntity(request);
        recipe.setRecipeKind(kind);
        if (kind == RecipeKind.GOLDEN) {
            if (request.getParentRecipeId() != null) {
                throw new IllegalArgumentException("GOLDEN recipe must not have a parentRecipeId");
            }
            recipe.setParentRecipe(null);
            recipe.setVersion(request.getVersion() == null ? 1 : request.getVersion());
        } else {
            Recipe parent = resolveDerivedParent(request.getParentRecipeId(), resultProfileId);
            recipe.setParentRecipe(parent);
            recipe.setVersion(nextDerivedVersion(parent.getId(), request.getVersion()));
        }

        if (recipe.getStatus() == null) {
            recipe.setStatus(RecipeStatus.DRAFT);
        }

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toDto(saved);
    }

    /**
     * Executes getRecipe.
     *
     * @param recipeId input argument consumed by getRecipe.
     * @return computed RecipeDto result returned by getRecipe.
     */
    @Transactional(readOnly = true)
    public RecipeDto getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        return recipeMapper.toDto(recipe);
    }

    /**
     * Executes updateRecipe.
     *
     * @param recipeId input argument consumed by updateRecipe.
     * @param request input argument consumed by updateRecipe.
     * @return computed RecipeDto result returned by updateRecipe.
     */
    @Transactional
    public RecipeDto updateRecipe(Long recipeId, RecipeDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Recipe payload is required");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        if (request.getRecipeKind() != null && request.getRecipeKind() != recipe.getRecipeKind()) {
            throw new IllegalArgumentException("recipeKind cannot be changed");
        }

        Long existingParentRecipeId = recipe.getParentRecipe() != null ? recipe.getParentRecipe().getId() : null;
        if (request.getParentRecipeId() != null && !request.getParentRecipeId().equals(existingParentRecipeId)) {
            throw new IllegalArgumentException("parentRecipeId cannot be changed");
        }

        if (request.getVersion() != null && !request.getVersion().equals(recipe.getVersion())) {
            throw new IllegalArgumentException("version cannot be changed");
        }

        if (request.getCreatorId() != null && !request.getCreatorId().equals(recipe.getCreatorId())) {
            throw new IllegalArgumentException("creatorId cannot be changed");
        }

        recipeMapper.updateEntityFromDto(request, recipe);

        if (recipe.getRecipeKind() == RecipeKind.GOLDEN) {
            recipe.setParentRecipe(null);
        }

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toDto(saved);
    }

    /**
     * Executes deleteRecipe.
     *
     * @param recipeId input argument consumed by deleteRecipe.
     */
    @Transactional
    public void deleteRecipe(Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new EntityNotFoundException("Recipe with id " + recipeId + " not found");
        }

        // A derived recipe can be linked by historical decision executions.
        // Clear that optional link before deleting the recipe entity.
        decisionExecutionRepository.clearCreatedDerivedRecipeReference(recipeId);

        recipeRepository.deleteById(recipeId);
    }

    /**
     * Executes listDerivedVersions.
     *
     * @param parentRecipeId input argument consumed by listDerivedVersions.
     * @return computed List<RecipeDto> result returned by listDerivedVersions.
     */
    @Transactional(readOnly = true)
    public List<RecipeDto> listDerivedVersions(Long parentRecipeId) {
        return recipeMapper.toDtoList(recipeRepository.findByParentRecipeIdOrderByVersionDesc(parentRecipeId));
    }

    private Recipe resolveDerivedParent(Long parentRecipeId, Long resultProfileId) {
        if (parentRecipeId != null) {
            return recipeRepository.findById(parentRecipeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent recipe with id " + parentRecipeId + " not found"));
        }

        if (resultProfileId == null) {
            throw new IllegalArgumentException(
                    "DERIVED recipe requires parentRecipeId or resultProfileId from questionnaire");
        }

        DecisionResultProfile profile = decisionResultProfileRepository.findById(resultProfileId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DecisionResultProfile with id " + resultProfileId + " not found"));

        if (profile.getGoldenRecipeId() == null) {
            throw new IllegalStateException(
                    "DecisionResultProfile " + resultProfileId + " has no goldenRecipeId");
        }

        return recipeRepository.findById(profile.getGoldenRecipeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Golden recipe with id " + profile.getGoldenRecipeId() + " not found"));
    }

    private Integer nextDerivedVersion(Long parentRecipeId, Integer requestedVersion) {
        if (requestedVersion != null) {
            return requestedVersion;
        }

        List<Recipe> versions = recipeRepository.findByParentRecipeIdOrderByVersionDesc(parentRecipeId);
        if (versions.isEmpty() || versions.get(0).getVersion() == null) {
            return 1;
        }
        return versions.get(0).getVersion() + 1;
    }
}
