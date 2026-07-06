package st.tt.script_back.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import st.tt.script_back.exceptions.BusinessCodeResolver;

/**
 * HTTP filter that adds a business-code response header for API calls.
 * <p>
 * After downstream processing, the filter computes a success code from request metadata and response status,
 * unless another component has already set the header.
 */
@Component
public class BusinessCodeFilter extends OncePerRequestFilter {

    /**
     * Applies the filter chain and conditionally sets {@code X-Business-Code} on API responses.
     *
     * @param request current HTTP request.
     * @param response current HTTP response.
     * @param filterChain downstream filter chain.
     * @throws ServletException when the servlet container reports a filter failure.
     * @throws IOException when input or output processing fails.
     */
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
