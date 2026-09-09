package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.Recipe;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;

/**
 * RecipeRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
            select distinct r
            from Recipe r
            left join fetch r.steps s
            left join fetch s.parameters sp
            where r.id = :id
            """)
    Optional<Recipe> findByIdWithStepsAndParameters(Long id);

    @Query("""
            select distinct r
            from Recipe r
            left join fetch r.steps s
            where r.id = :id
            """)
    Optional<Recipe> findByIdWithSteps(Long id);

        @Query("""
            select distinct r
            from Recipe r
            left join fetch r.requiredCapabilities
            left join fetch r.requiredConfigurationDefinitions
            where r.id = :recipeId
            """)
        Optional<Recipe> findByIdWithRequirements(Long recipeId);

    List<Recipe> findByStatusOrderByReviseTimeDesc(RecipeStatus status);

    List<Recipe> findByRecipeKindOrderByReviseTimeDesc(RecipeKind recipeKind);

    List<Recipe> findByRecipeKindAndCreatorIdOrderByReviseTimeDesc(RecipeKind recipeKind, Long creatorId);

    List<Recipe> findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind recipeKind);

    List<Recipe> findByParentRecipeIdOrderByVersionDesc(Long parentRecipeId);

}
