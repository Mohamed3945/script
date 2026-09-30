package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionResultProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * DecisionResultProfileRepository interface for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
public interface DecisionResultProfileRepository extends JpaRepository<DecisionResultProfile, Long> {

    List<DecisionResultProfile> findAllByOrderByCodeAsc();

    Optional<DecisionResultProfile> findByCode(String code);

    boolean existsByCode(String code);
    
}
