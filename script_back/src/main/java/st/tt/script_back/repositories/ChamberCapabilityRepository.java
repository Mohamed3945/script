package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ChamberCapability;

public interface ChamberCapabilityRepository extends JpaRepository<ChamberCapability, Long> {

    List<ChamberCapability> findAllByOrderByCategoryAscLabelAsc();

    boolean existsByCode(String code);

    boolean existsByLabel(String label);
}
