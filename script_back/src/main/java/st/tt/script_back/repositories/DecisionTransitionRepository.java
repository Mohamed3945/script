package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionOption;
import st.tt.script_back.entities.DecisionQuestion;
import st.tt.script_back.entities.DecisionTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DecisionTransitionRepository extends JpaRepository<DecisionTransition, Long> {
    
    Optional<DecisionTransition> findByCurrentQuestionIdAndOptionId(Long currentQuestionId, Long optionId);
}
