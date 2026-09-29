package st.tt.script_back.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentifie les requêtes HTTP à partir de leur en-tête {@code Authorization}.
 *
 * <p>Le filtre attend le format {@code Bearer <jeton>}. Il charge ensuite
 * l'utilisateur correspondant au sujet du JWT et ne place une authentification
 * dans le contexte de sécurité qu'après validation de la signature et de la date
 * d'expiration. Un jeton invalide est ignoré et le contexte est nettoyé.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;

    /**
     * Construit le filtre JWT.
     *
     * @param jwtService service de lecture et de validation des jetons
     * @param appUserDetailsService service de chargement des utilisateurs
     */
    public JwtAuthenticationFilter(JwtService jwtService, AppUserDetailsService appUserDetailsService) {
        this.jwtService = jwtService;
        this.appUserDetailsService = appUserDetailsService;
    }

    /**
     * Traite l'authentification éventuelle avant de poursuivre la chaîne HTTP.
     *
     * <p>Les requêtes sans en-tête Bearer continuent sans authentification créée
     * par ce filtre. Les erreurs de format ou de validation du jeton ne bloquent
     * pas directement la chaîne ; elles empêchent seulement l'établissement du
     * contexte authentifié.</p>
     *
     * @param request requête HTTP entrante
     * @param response réponse HTTP en cours de construction
     * @param filterChain chaîne de filtres à poursuivre
     * @throws ServletException si un filtre suivant signale une erreur de servlet
     * @throws IOException si un filtre suivant rencontre une erreur d'entrée/sortie
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AppUserDetails userDetails = (AppUserDetails) appUserDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}