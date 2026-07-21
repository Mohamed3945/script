package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.StepEndpoint;

public interface StepEndpointRepository extends JpaRepository<StepEndpoint, Long> {

    @Query("""
        select e from StepEndpoint e
        left join fetch e.conditions c
        left join fetch c.endpointParameter ep
        left join fetch c.selectedOption so
        where e.step.id = :stepId
        order by c.orderIndex asc
    """)
    Optional<StepEndpoint> findByStepIdWithConditions(@Param("stepId") Long stepId);

    @Query("""
        select distinct e from StepEndpoint e
        left join fetch e.conditions c
        left join fetch c.endpointParameter ep
        left join fetch c.selectedOption so
        where e.step.id in :stepIds
    """)
    List<StepEndpoint> findByStepIdsWithConditions(@Param("stepIds") List<Long> stepIds);

    boolean existsByStepId(Long stepId);
}