package st.tt.script_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import st.tt.script_back.security.AppUserDetailsService;
import st.tt.script_back.security.JwtAuthenticationFilter;

/**
 * Configure la sécurité HTTP de l'API.
 *
 * <p>L'application est sans session : chaque requête protégée doit présenter un
 * jeton JWT valide. Les opérations de lecture sont accessibles aux rôles
 * {@code SIMPLE} et {@code SUPER}, tandis que les opérations d'administration
 * nécessitent généralement le rôle {@code SUPER}.</p>
 *
 * <p>La chaîne de filtres laisse notamment publics la connexion, la documentation
 * OpenAPI et les endpoints d'information de santé.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AppUserDetailsService appUserDetailsService;

    /**
     * Construit la configuration de sécurité.
     *
     * @param jwtAuthenticationFilter filtre qui authentifie les requêtes porteuses d'un JWT
     * @param appUserDetailsService service de chargement des utilisateurs applicatifs
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AppUserDetailsService appUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.appUserDetailsService = appUserDetailsService;
    }

    /**
     * Déclare la chaîne de filtres appliquée à toutes les requêtes HTTP.
     *
     * @param http objet de configuration Spring Security
     * @return chaîne de filtres construite
     * @throws Exception si Spring Security ne peut pas construire la chaîne
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/users/**").hasRole("SUPER")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/transitions/**").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/decision-executions/finalize").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/recipe-compatibility/compatible-machines").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.PUT, "/api/recipes/*").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/*").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.PUT, "/api/step-parameters/**").hasAnyRole("SIMPLE", "SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("SUPER")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("SUPER")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("SUPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("SUPER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure l'authentification par identifiant et mot de passe.
     *
     * @return fournisseur utilisant les utilisateurs applicatifs et BCrypt
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(appUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expose le gestionnaire d'authentification construit par Spring.
     *
     * @param configuration configuration d'authentification Spring
     * @return gestionnaire d'authentification
     * @throws Exception si le gestionnaire ne peut pas être obtenu
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Fournit l'encodeur utilisé pour les mots de passe persistés.
     *
     * @return encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}