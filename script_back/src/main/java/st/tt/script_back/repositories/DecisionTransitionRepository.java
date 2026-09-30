package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * DecisionTransitionRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface DecisionTransitionRepository extends JpaRepository<DecisionTransition, Long> {

    List<DecisionTransition> findAllByOrderByCurrentQuestionOrderIndexAscOptionOrderIndexAsc();

    void deleteByOptionId(Long optionId);

    void deleteByCurrentQuestionIdOrNextQuestionId(Long currentQuestionId, Long nextQuestionId);

    void deleteByResultProfileId(Long resultProfileId);
    
    Optional<DecisionTransition> findByCurrentQuestionIdAndOptionId(Long currentQuestionId, Long optionId);
}
