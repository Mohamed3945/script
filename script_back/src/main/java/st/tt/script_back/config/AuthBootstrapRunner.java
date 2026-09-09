package st.tt.script_back.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import st.tt.script_back.entities.AppRole;
import st.tt.script_back.entities.AppUser;
import st.tt.script_back.enums.RoleCode;
import st.tt.script_back.repositories.AppRoleRepository;
import st.tt.script_back.repositories.AppUserRepository;

@Component
public class AuthBootstrapRunner implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String password;
    private final String displayName;

    public AuthBootstrapRunner(
            AppUserRepository appUserRepository,
            AppRoleRepository appRoleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${script.security.bootstrap.enabled:true}") boolean enabled,
            @Value("${script.security.bootstrap.username:admin}") String username,
            @Value("${script.security.bootstrap.password:ChangeMeDev2026!}") String password,
            @Value("${script.security.bootstrap.display-name:Administrateur}") String displayName) {
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        AppRole superRole = appRoleRepository.findByCode(RoleCode.SUPER)
                .orElseThrow(() -> new IllegalStateException("SUPER role is missing"));

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        user.setRoles(Set.of(superRole));
        appUserRepository.save(user);
    }
}