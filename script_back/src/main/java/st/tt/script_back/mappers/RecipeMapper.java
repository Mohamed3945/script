package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.RecipeDeepDetailDto;
import st.tt.script_back.dto.RecipeDetailDto;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.StepDetailDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.entities.Recipe;

/**
 * RecipeMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class RecipeMapper {

    /**
     * Executes toDto.
     *
     * @param recipe input argument consumed by toDto.
     * @return computed RecipeDto result returned by toDto.
     */
    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        return new RecipeDto(
                recipe.getId(),
                recipe.getRecipeKind(),
                recipe.getParentRecipe() != null ? recipe.getParentRecipe().getId() : null,
                recipe.getName(),
                recipe.getDescription(),
                recipe.getCreatorId(),
                recipe.getRevisorId(),
                recipe.getProcessFamily(),
                recipe.getStatus(),
                recipe.getVersion(),
                recipe.isFrozen()
        );
    }

    /**
     * Executes toDetailDto.
     *
     * @param recipe input argument consumed by toDetailDto.
     * @param steps input argument consumed by toDetailDto.
     * @return computed RecipeDetailDto result returned by toDetailDto.
     */
    public RecipeDetailDto toDetailDto(Recipe recipe, List<StepDto> steps) {
        if (recipe == null) {
            return null;
        }
        return new RecipeDetailDto(
                recipe.getId(),
                recipe.getRecipeKind(),
                recipe.getParentRecipe() != null ? recipe.getParentRecipe().getId() : null,
                recipe.getName(),
                recipe.getDescription(),
                recipe.getCreatorId(),
                recipe.getRevisorId(),
                recipe.getProcessFamily(),
                recipe.getStatus(),
                recipe.getVersion(),
                recipe.isFrozen(),
                steps == null ? Collections.emptyList() : steps
        );
    }

    /**
     * Executes toDeepDetailDto.
     *
     * @param recipe input argument consumed by toDeepDetailDto.
     * @param steps input argument consumed by toDeepDetailDto.
     * @return computed RecipeDeepDetailDto result returned by toDeepDetailDto.
     */
    public RecipeDeepDetailDto toDeepDetailDto(Recipe recipe, List<StepDetailDto> steps) {
        if (recipe == null) {
            return null;
        }
        return new RecipeDeepDetailDto(
                recipe.getId(),
                recipe.getRecipeKind(),
                recipe.getParentRecipe() != null ? recipe.getParentRecipe().getId() : null,
                recipe.getName(),
                recipe.getDescription(),
                recipe.getCreatorId(),
                recipe.getRevisorId(),
                recipe.getProcessFamily(),
                recipe.getStatus(),
                recipe.getVersion(),
                recipe.isFrozen(),
                steps == null ? Collections.emptyList() : steps
        );
    }

    /**
     * Executes toDtoList.
     *
     * @param recipes input argument consumed by toDtoList.
     * @return computed List<RecipeDto> result returned by toDtoList.
     */
    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        if (recipes == null) {
            return Collections.emptyList();
        }
        return recipes.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed Recipe result returned by toEntity.
     */
    public Recipe toEntity(RecipeDto dto) {
        if (dto == null) {
            return null;
        }
        Recipe entity = new Recipe();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    /**
     * Executes updateEntityFromDto.
     *
     * @param dto input argument consumed by updateEntityFromDto.
     * @param entity input argument consumed by updateEntityFromDto.
     */
    public void updateEntityFromDto(RecipeDto dto, Recipe entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCreatorId(dto.getCreatorId());
        entity.setRevisorId(dto.getRevisorId());
        entity.setProcessFamily(dto.getProcessFamily());
        entity.setStatus(dto.getStatus());
        entity.setFrozen(dto.isFrozen());
    }
}
