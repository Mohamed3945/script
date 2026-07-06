package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.Step;
import st.tt.script_back.enums.StepKind;

/**
 * StepRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface StepRepository extends JpaRepository<Step, Long> {

    List<Step> findByRecipeIdOrderByOrderIndexAsc(Long recipeId);

    List<Step> findByRecipeIdAndStepKindOrderByOrderIndexAsc(Long recipeId, StepKind stepKind);

    Optional<Step> findByRecipeIdAndCode(Long recipeId, String code);

    boolean existsByRecipeIdAndCode(Long recipeId, String code);

    Step findTopByRecipeIdOrderByOrderIndexDesc(Long recipeId);

}
