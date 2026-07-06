package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * DecisionTransitionRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface DecisionTransitionRepository extends JpaRepository<DecisionTransition, Long> {
    
    Optional<DecisionTransition> findByCurrentQuestionIdAndOptionId(Long currentQuestionId, Long optionId);
}
