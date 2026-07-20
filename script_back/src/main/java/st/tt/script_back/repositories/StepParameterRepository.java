package st.tt.script_back.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.StepKind;

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

    List<StepParameter> findByStepRecipeIdAndDefinitionId(Long recipeId, Long definitionId);

    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch d.parameterGroupRef pg
            left join fetch sp.selectedOption so
            left join fetch sp.parentStepParameter psp
            left join fetch psp.selectedOption pso
            where sp.step.id = :stepId
            order by sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findByStepIdWithDefinitionAndSelectedOption(@Param("stepId") Long stepId);

    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch d.parameterGroupRef pg
            left join fetch sp.selectedOption so
            left join fetch sp.parentStepParameter psp
            left join fetch psp.selectedOption pso
            where sp.step.id in :stepIds
            order by sp.step.id asc, sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findByStepIdsWithDefinitionAndSelectedOption(@Param("stepIds") List<Long> stepIds);

    @Query("""
                select distinct sp.step.recipe.id
                from StepParameter sp
                where sp.definition.id in :definitionIds
                """)
    List<Long> findDistinctRecipeIdsByDefinitionIds(@Param("definitionIds") List<Long> definitionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                        update StepParameter sp
                        set sp.userModified = false
                        where sp.step.recipe.id = :recipeId
                        """)
    int resetUserModifiedByRecipeId(@Param("recipeId") Long recipeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from StepParameter sp
            where sp.step.recipe.id = :recipeId
              and sp.definition.id = :definitionId
            """)
    int deleteByRecipeIdAndDefinitionId(@Param("recipeId") Long recipeId, @Param("definitionId") Long definitionId);

    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch d.parameterGroupRef pg
            left join fetch sp.selectedOption so
            left join fetch sp.parentStepParameter psp
            left join fetch psp.selectedOption pso
            where sp.step.id = :stepId
            order by sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findForStepClone(@Param("stepId") Long stepId);


    @Query("""
            select sp
            from StepParameter sp
            join fetch sp.step s
            join fetch sp.definition d
            left join fetch d.configurationDefinition cd
            left join fetch sp.selectedOption so
            where s.recipe.id = :recipeId
              and s.stepKind in :stepKinds
              and sp.activationState <> :disabledState
            order by s.orderIndex asc, sp.parentOrderScope asc, sp.orderIndex asc
            """)
    List<StepParameter> findActiveCompatibilityParametersByRecipeId(
            @Param("recipeId") Long recipeId,
            @Param("stepKinds") Set<StepKind> stepKinds,
            @Param("disabledState") ActivationState disabledState);
}