package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionResultProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DecisionResultProfileRepository extends JpaRepository<DecisionResultProfile, Long> {

    Optional<DecisionResultProfile> findByCode(String code);
    
}
