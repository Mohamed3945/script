package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * DecisionOptionRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface DecisionOptionRepository extends JpaRepository<DecisionOption, Long> {

    List<DecisionOption> findByDecisionQuestionIdOrderByOrderIndexAsc(Long questionId);

}
