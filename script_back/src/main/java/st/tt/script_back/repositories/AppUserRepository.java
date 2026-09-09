package st.tt.script_back.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.AppUser;
import st.tt.script_back.enums.RoleCode;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select count(u) from AppUser u join u.roles r where u.active = true and r.code = :roleCode")
    long countActiveUsersByRole(@Param("roleCode") RoleCode roleCode);
}