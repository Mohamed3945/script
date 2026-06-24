package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterDependencyRule;

public interface ParameterDependencyRuleRepository extends JpaRepository<ParameterDependencyRule, Long> {

    List<ParameterDependencyRule> findBySourceDefinitionIdOrderByPriorityAscIdAsc(Long sourceDefinitionId);

    List<ParameterDependencyRule> findByTargetDefinitionIdOrderByPriorityAscIdAsc(Long targetDefinitionId);

    List<ParameterDependencyRule> findBySourceDefinitionIdAndTargetDefinitionIdOrderByPriorityAscIdAsc(Long sourceDefinitionId, Long targetDefinitionId);

}
