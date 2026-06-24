package st.tt.script_back.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.ParameterDefinition;

public interface ParameterDefinitionRepository extends JpaRepository<ParameterDefinition, Long> {

    Optional<ParameterDefinition> findByCode(String code);

    boolean existsByCode(String code);

    Optional<ParameterDefinition> findByAlias(String alias);

    Optional<ParameterDefinition> findByName(String name);

}
