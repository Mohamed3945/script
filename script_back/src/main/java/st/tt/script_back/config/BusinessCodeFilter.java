package st.tt.script_back.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import st.tt.script_back.exceptions.BusinessCodeResolver;

@Component
public class BusinessCodeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return;
        }

        if (response.getHeader(BusinessCodeResolver.HEADER_NAME) != null) {
            return;
        }

        String code = BusinessCodeResolver.resolveSuccessCode(
                request.getMethod(),
                path,
                response.getStatus());

        if (code != null) {
            response.setHeader(BusinessCodeResolver.HEADER_NAME, code);
        }
    }
}
