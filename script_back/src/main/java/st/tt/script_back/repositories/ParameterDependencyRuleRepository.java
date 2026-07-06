package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import st.tt.script_back.entities.ParameterDependencyRule;

/**
 * ParameterDependencyRuleRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface ParameterDependencyRuleRepository extends JpaRepository<ParameterDependencyRule, Long> {

    List<ParameterDependencyRule> findBySourceDefinitionIdOrderByPriorityAscIdAsc(Long sourceDefinitionId);

    List<ParameterDependencyRule> findByTargetDefinitionIdOrderByPriorityAscIdAsc(Long targetDefinitionId);

    List<ParameterDependencyRule> findBySourceDefinitionIdAndTargetDefinitionIdOrderByPriorityAscIdAsc(Long sourceDefinitionId, Long targetDefinitionId);

        @Query("""
            select r
            from ParameterDependencyRule r
            left join fetch r.sourceDefinition
            left join fetch r.triggerOption
            left join fetch r.requiredSourceActivationOption
            left join fetch r.targetDefinition
            order by r.priority asc, r.id asc
            """)
        List<ParameterDependencyRule> findAllForView();

        @Query("""
            select r
            from ParameterDependencyRule r
            left join fetch r.sourceDefinition
            left join fetch r.triggerOption
            left join fetch r.requiredSourceActivationOption
            left join fetch r.targetDefinition
            where r.id = :id
            """)
        Optional<ParameterDependencyRule> findByIdForView(Long id);

}
