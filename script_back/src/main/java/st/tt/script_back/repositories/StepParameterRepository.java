package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.StepParameter;

public interface StepParameterRepository extends JpaRepository<StepParameter, Long> {

    List<StepParameter> findByStepIdOrderByParentOrderScopeAscOrderIndexAsc(Long stepId);

    List<StepParameter> findByParentStepParameterIdOrderByOrderIndexAsc(Long parentStepParameterId);

    List<StepParameter> findByStepIdAndParentOrderScopeOrderByOrderIndexAsc(Long stepId, Long parentOrderScope);

        @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch sp.selectedOption so
            where sp.step.id = :stepId
            order by sp.parentOrderScope asc, sp.orderIndex asc
            """)
        List<StepParameter> findByStepIdWithDefinitionAndSelectedOption(@Param("stepId") Long stepId);

        @Query("""
            select sp
            from StepParameter sp
            join fetch sp.definition d
            left join fetch sp.selectedOption so
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

}
