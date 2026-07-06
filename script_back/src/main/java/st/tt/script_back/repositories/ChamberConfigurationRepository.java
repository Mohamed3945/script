package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.ChamberConfiguration;

public interface ChamberConfigurationRepository extends JpaRepository<ChamberConfiguration, Long> {

    @Query("""
        select cc
        from ChamberConfiguration cc
        join fetch cc.configurationDefinition cd
        where cc.chamber.id = :chamberId
        order by cd.code asc, cc.code asc
    """)
    List<ChamberConfiguration> findByChamberIdOrdered(@Param("chamberId") Long chamberId);

    boolean existsByChamberIdAndConfigurationDefinitionIdAndCode(Long chamberId, Long configurationDefinitionId, String code);
}
