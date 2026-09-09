package st.tt.script_back.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import st.tt.script_back.dto.CurrentUserDto;
import st.tt.script_back.dto.LoginRequestDto;
import st.tt.script_back.dto.LoginResponseDto;
import st.tt.script_back.enums.RoleCode;
import st.tt.script_back.mappers.AppUserMapper;
import st.tt.script_back.security.AppUserDetails;
import st.tt.script_back.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserMapper appUserMapper;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AppUserMapper appUserMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserMapper = appUserMapper;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        return new LoginResponseDto(jwtService.generateToken(userDetails), appUserMapper.toCurrentUserDto(userDetails.getUser()));
    }

    public CurrentUserDto getCurrentUser() {
        AppUserDetails userDetails = getCurrentUserDetails();
        return appUserMapper.toCurrentUserDto(userDetails.getUser());
    }

    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public boolean hasRole(RoleCode roleCode) {
        return getCurrentUserDetails().getUser().getRoles().stream()
                .anyMatch(role -> role.getCode() == roleCode);
    }

    public boolean isSuperUser() {
        return hasRole(RoleCode.SUPER);
    }

    private AppUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }
        return userDetails;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}