package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import st.tt.script_back.entities.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findAllByOrderByReviseTimeDesc();

    boolean existsByCode(String code);

    boolean existsByName(String name);

    @Query("""
        select distinct m
        from Machine m
        left join fetch m.chambers c
        where m.id = :id
        order by c.code asc
    """)
    Optional<Machine> findByIdWithChambers(Long id);

    Optional<Machine> findFirstByOrderByIdAsc();
}
