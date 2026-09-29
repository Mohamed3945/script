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

/** Contrôleur d'authentification et d'accès aux informations de l'utilisateur courant. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /** @param authService service de connexion et de gestion de l'utilisateur courant */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authentifie un utilisateur et retourne son jeton JWT.
     * @param request identifiants de connexion
     * @return jeton et informations de session
     */
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    /**
     * Retourne le profil associé au JWT courant.
     * @return informations de l'utilisateur authentifié
     */
    @GetMapping("/me")
    public CurrentUserDto getCurrentUser() {
        return authService.getCurrentUser();
    }
}