package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.CurrentUserDto;
import st.tt.script_back.dto.LoginRequestDto;
import st.tt.script_back.dto.LoginResponseDto;
import st.tt.script_back.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserDto getCurrentUser() {
        return authService.getCurrentUser();
    }
}