package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.AppUserDto;
import st.tt.script_back.dto.CurrentUserDto;
import st.tt.script_back.entities.AppRole;
import st.tt.script_back.entities.AppUser;
import st.tt.script_back.enums.RoleCode;

@Component
public class AppUserMapper {

    public AppUserDto toDto(AppUser user) {
        if (user == null) {
            return null;
        }
        return new AppUserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.isActive(),
                toRoleCodes(user));
    }

    public CurrentUserDto toCurrentUserDto(AppUser user) {
        if (user == null) {
            return null;
        }
        return new CurrentUserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.isActive(),
                toRoleCodes(user));
    }

    private Set<RoleCode> toRoleCodes(AppUser user) {
        if (user.getRoles() == null) {
            return Collections.emptySet();
        }
        return user.getRoles().stream()
                .map(AppRole::getCode)
                .collect(Collectors.toSet());
    }
}