package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

        @Query("""
            select count(r) > 0
            from ParameterDependencyRule r
            where r.sourceDefinition.id = :sourceDefinitionId
              and r.triggerOption.id = :triggerOptionId
              and (
                (:requiredSourceActivationOptionId is null and r.requiredSourceActivationOption is null)
                or r.requiredSourceActivationOption.id = :requiredSourceActivationOptionId
              )
              and r.targetDefinition.id = :targetDefinitionId
              and (:excludeId is null or r.id <> :excludeId)
            """)
        boolean existsRuleCombination(
            @Param("sourceDefinitionId") Long sourceDefinitionId,
            @Param("triggerOptionId") Long triggerOptionId,
            @Param("requiredSourceActivationOptionId") Long requiredSourceActivationOptionId,
            @Param("targetDefinitionId") Long targetDefinitionId,
            @Param("excludeId") Long excludeId);

}
