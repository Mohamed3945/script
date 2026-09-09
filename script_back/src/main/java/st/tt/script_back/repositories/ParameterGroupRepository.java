package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterGroup;
import st.tt.script_back.enums.ParameterScope;

public interface ParameterGroupRepository extends JpaRepository<ParameterGroup, Long> {

    List<ParameterGroup> findByStepTypeOrderByOrderIndexAsc(ParameterScope stepType);

    Optional<ParameterGroup> findTopByStepTypeOrderByOrderIndexDesc(ParameterScope stepType);

    Optional<ParameterGroup> findByStepTypeAndSystemGroupTrue(ParameterScope stepType);

    List<ParameterGroup> findByIdIn(List<Long> ids);
}

