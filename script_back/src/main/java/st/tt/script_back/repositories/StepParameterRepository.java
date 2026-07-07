package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.StepParameter;

/**
 * Repository for step parameter persistence and projection-oriented fetch queries.
 * <p>
 * Custom queries eagerly fetch definition, selected option, and parent selected option to avoid N+1 lookups
 * in activation and matrix computation workflows.
 */
public interface StepParameterRepository extends JpaRepository<StepParameter, Long> {

    boolean existsByDefinitionId(Long definitionId);

    boolean existsBySelectedOptionDefinitionId(Long definitionId);

    List<StepParameter> findByStepIdOrderByParentOrderScopeAscOrderIndexAsc(Long stepId);

    List<StepParameter> findByParentStepParameterIdOrderByOrderIndexAsc(Long parentStepParameterId);

    List<StepParameter> findByStepIdAndParentOrderScopeOrderByOrderIndexAsc(Long stepId, Long parentOrderScope);

    /**
     * Loads all parameters of one step with associations required by activation and UI mapping.
     *
     * @param stepId step identifier.
     * @return ordered parameters with definition, selected option, parent parameter, and parent selected option.
     */
    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch sp.selectedOption so
            left join fetch sp.parentStepParameter psp
            left join fetch psp.selectedOption pso
            where sp.step.id = :stepId
            order by sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findByStepIdWithDefinitionAndSelectedOption(@Param("stepId") Long stepId);

    /**
     * Loads parameters for a set of steps with all associations needed by activation and matrix builders.
     *
     * @param stepIds ordered or unordered step identifiers.
     * @return step parameters sorted by step and within-step order.
     */
    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch sp.selectedOption so
            left join fetch sp.parentStepParameter psp
            left join fetch psp.selectedOption pso
            where sp.step.id in :stepIds
            order by sp.step.id asc, sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findByStepIdsWithDefinitionAndSelectedOption(@Param("stepIds") List<Long> stepIds);

    /**
     * Finds all recipes containing at least one parameter linked to any of the provided definitions.
     *
     * @param definitionIds parameter definition identifiers.
     * @return distinct recipe identifiers.
     */
    @Query("""
                select distinct sp.step.recipe.id
                from StepParameter sp
                where sp.definition.id in :definitionIds
                """)
    List<Long> findDistinctRecipeIdsByDefinitionIds(@Param("definitionIds") List<Long> definitionIds);

        /**
         * Resets user-modification markers for all parameters of one recipe.
         * <p>
         * Used after golden-to-derived cloning to ensure the new derived recipe starts with a clean editable state.
         *
         * @param recipeId recipe identifier.
         * @return number of updated parameters.
         */
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        update StepParameter sp
                        set sp.userModified = false
                        where sp.step.recipe.id = :recipeId
                        """)
        int resetUserModifiedByRecipeId(@Param("recipeId") Long recipeId);

}
