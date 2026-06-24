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

@Component
public class RecipeMapper {

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

    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        if (recipes == null) {
            return Collections.emptyList();
        }
        return recipes.stream().map(this::toDto).toList();
    }

    public Recipe toEntity(RecipeDto dto) {
        if (dto == null) {
            return null;
        }
        Recipe entity = new Recipe();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

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
