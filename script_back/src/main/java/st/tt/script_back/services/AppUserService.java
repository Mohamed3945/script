package st.tt.script_back.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.AppUserDto;
import st.tt.script_back.dto.ChangePasswordRequestDto;
import st.tt.script_back.dto.CreateUserRequestDto;
import st.tt.script_back.dto.UpdateUserRequestDto;
import st.tt.script_back.entities.AppRole;
import st.tt.script_back.entities.AppUser;
import st.tt.script_back.enums.RoleCode;
import st.tt.script_back.mappers.AppUserMapper;
import st.tt.script_back.repositories.AppRoleRepository;
import st.tt.script_back.repositories.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;

    public AppUserService(
            AppUserRepository appUserRepository,
            AppRoleRepository appRoleRepository,
            PasswordEncoder passwordEncoder,
            AppUserMapper appUserMapper) {
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
    }

    @Transactional(readOnly = true)
    public List<AppUserDto> getUsers() {
        return appUserRepository.findAll().stream()
                .map(appUserMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserDto getUser(Long id) {
        return appUserMapper.toDto(findUser(id));
    }

    @Transactional
    public AppUserDto createUser(CreateUserRequestDto request) {
        validateCreateRequest(request);
        String username = request.getUsername().trim();
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!isBlank(request.getEmail()) && appUserRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(trimToNull(request.getDisplayName()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setActive(request.isActive());
        user.setRoles(resolveRoles(request.getRoles()));

        return appUserMapper.toDto(appUserRepository.save(user));
    }

    @Transactional
    public AppUserDto updateUser(Long id, UpdateUserRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("User payload is required");
        }
        AppUser user = findUser(id);
        if (!isBlank(request.getEmail())
                && !request.getEmail().trim().equalsIgnoreCase(user.getEmail())
                && appUserRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setDisplayName(trimToNull(request.getDisplayName()));
        user.setEmail(trimToNull(request.getEmail()));
        if (request.getRoles() != null) {
            user.setRoles(resolveRoles(request.getRoles()));
        }
        if (request.getActive() != null && request.getActive() != user.isActive()) {
            ensureCanChangeActiveState(user, request.getActive());
            user.setActive(request.getActive());
        }

        return appUserMapper.toDto(appUserRepository.save(user));
    }

    @Transactional
    public AppUserDto changePassword(Long id, ChangePasswordRequestDto request) {
        if (request == null || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
        AppUser user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return appUserMapper.toDto(appUserRepository.save(user));
    }

    private AppUser findUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    private void validateCreateRequest(CreateUserRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("User payload is required");
        }
        if (isBlank(request.getUsername())) {
            throw new IllegalArgumentException("Username is required");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }

    private Set<AppRole> resolveRoles(Set<RoleCode> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        List<AppRole> roles = appRoleRepository.findByCodeIn(requestedRoles);
        if (roles.size() != requestedRoles.size()) {
            throw new IllegalArgumentException("Unknown role in request");
        }
        return new HashSet<>(roles);
    }

    private void ensureCanChangeActiveState(AppUser user, boolean nextActive) {
        if (nextActive || !hasRole(user, RoleCode.SUPER)) {
            return;
        }
        if (appUserRepository.countActiveUsersByRole(RoleCode.SUPER) <= 1) {
            throw new IllegalStateException("Cannot disable the last active SUPER user");
        }
    }

    private boolean hasRole(AppUser user, RoleCode roleCode) {
        return user.getRoles().stream().anyMatch(role -> role.getCode() == roleCode);
    }

    private String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}