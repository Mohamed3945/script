package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.FormulaReference;

public interface FormulaReferenceRepository extends JpaRepository<FormulaReference, Long> {

    List<FormulaReference> findByFormulaIdOrderBySlotAsc(Long formulaId);

    List<FormulaReference> findByStepCodeAndDefinitionPath(String stepCode, String definitionPath);

    boolean existsByStepCodeAndDefinitionPath(String stepCode, String definitionPath);
}
