package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterOption;

/**
 * ParameterOptionRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface ParameterOptionRepository extends JpaRepository<ParameterOption, Long> {

    List<ParameterOption> findByDefinitionIdOrderByOrderIndexAsc(Long definitionId);

    Optional<ParameterOption> findByDefinitionIdAndCode(Long definitionId, String code);

    boolean existsByDefinitionIdAndCode(Long definitionId, String code);

}
