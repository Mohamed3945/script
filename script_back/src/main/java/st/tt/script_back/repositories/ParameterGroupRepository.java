package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterGroup;
import st.tt.script_back.enums.StepType;

public interface ParameterGroupRepository extends JpaRepository<ParameterGroup, Long> {

    List<ParameterGroup> findByStepTypeOrderByOrderIndexAsc(StepType stepType);

    Optional<ParameterGroup> findTopByStepTypeOrderByOrderIndexDesc(StepType stepType);

    List<ParameterGroup> findByIdIn(List<Long> ids);
}