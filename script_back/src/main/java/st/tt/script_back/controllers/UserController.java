package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.AppUserDto;
import st.tt.script_back.dto.ChangePasswordRequestDto;
import st.tt.script_back.dto.CreateUserRequestDto;
import st.tt.script_back.dto.UpdateUserRequestDto;
import st.tt.script_back.services.AppUserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUserDto> getUsers() {
        return appUserService.getUsers();
    }

    @GetMapping("/{id}")
    public AppUserDto getUser(@PathVariable Long id) {
        return appUserService.getUser(id);
    }

    @PostMapping
    public AppUserDto createUser(@RequestBody CreateUserRequestDto request) {
        return appUserService.createUser(request);
    }

    @PutMapping("/{id}")
    public AppUserDto updateUser(@PathVariable Long id, @RequestBody UpdateUserRequestDto request) {
        return appUserService.updateUser(id, request);
    }

    @PutMapping("/{id}/password")
    public AppUserDto changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequestDto request) {
        return appUserService.changePassword(id, request);
    }
}