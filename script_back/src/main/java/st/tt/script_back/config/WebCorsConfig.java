package st.tt.script_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebCorsConfig class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * Executes addCorsMappings.
     *
     * @param registry input argument consumed by addCorsMappings.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
            .exposedHeaders("X-Business-Code")
                .maxAge(3600);
    }
}
