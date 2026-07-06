package st.tt.script_back.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ConfigurationDefinition;

public interface ConfigurationDefinitionRepository extends JpaRepository<ConfigurationDefinition, Long> {

    List<ConfigurationDefinition> findAllByOrderByDisplayOrderAscCodeAsc();

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
