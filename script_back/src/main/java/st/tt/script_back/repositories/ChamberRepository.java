package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import st.tt.script_back.entities.Chamber;

public interface ChamberRepository extends JpaRepository<Chamber, Long> {

    List<Chamber> findByMachineIdOrderByCodeAsc(Long machineId);

    boolean existsByMachineIdAndCode(Long machineId, String code);

    boolean existsByMachineIdAndName(Long machineId, String name);

    @Query("""
        select distinct c
        from Chamber c
        left join fetch c.machine
        left join fetch c.capabilities
        left join fetch c.configurations cfg
        left join fetch cfg.configurationDefinition
        where c.id = :id
    """)
    Optional<Chamber> findByIdWithMachineCapabilitiesAndConfigurations(Long id);

    @Query("""
        select distinct c
        from Chamber c
        left join fetch c.machine
        left join fetch c.capabilities
        left join fetch c.configurations cfg
        left join fetch cfg.configurationDefinition
    """)
    List<Chamber> findAllWithMachineCapabilitiesAndConfigurations();
}
