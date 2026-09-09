package st.tt.script_back.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResponseStatusException_shouldPreserveConflictStatusAndReason() {
        when(request.getRequestURI()).thenReturn("/api/recipes/46/export/xml");

        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "XML export blocked by strict mode");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("XML export blocked by strict mode", response.getBody().get("message"));
        assertEquals(409, response.getBody().get("status"));
        assertNotNull(response.getHeaders().getFirst(BusinessCodeResolver.HEADER_NAME));
    }

    @Test
    void handleResponseStatusException_shouldFallbackTo500WhenStatusCodeIsUnknown() {
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "generic");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("generic", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
    }

    @Test
    void handleAllExceptions_shouldReturn500Payload() {
        when(request.getRequestURI()).thenReturn("/api/recipes");

        ResponseEntity<Map<String, Object>> response = handler.handleAllExceptions(
                new RuntimeException("boom"),
                request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
        assertEquals("/api/recipes", response.getBody().get("path"));
    }
}
