package st.tt.script_back.repositories;

import java.util.Optional;
import java.util.List;
import st.tt.script_back.enums.StepType;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterDefinition;

/**
 * ParameterDefinitionRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface ParameterDefinitionRepository extends JpaRepository<ParameterDefinition, Long> {

    Optional<ParameterDefinition> findByCode(String code);

    Optional<ParameterDefinition> findByCodeAndStepType(String code, StepType stepType);

    boolean existsByCode(String code);

    Optional<ParameterDefinition> findByAlias(String alias);

    Optional<ParameterDefinition> findByName(String name);

    List<ParameterDefinition> findByStepTypeOrderByNameAsc(StepType stepType);

}
