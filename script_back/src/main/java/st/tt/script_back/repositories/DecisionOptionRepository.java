package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DecisionOptionRepository extends JpaRepository<DecisionOption, Long> {

    List<DecisionOption> findByDecisionQuestionIdOrderByOrderIndexAsc(Long questionId);
    
}
