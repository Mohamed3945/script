package st.tt.script_back.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.AppRole;
import st.tt.script_back.enums.RoleCode;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByCode(RoleCode code);

    List<AppRole> findByCodeIn(Collection<RoleCode> codes);
}