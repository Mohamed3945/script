package st.tt.script_back.exceptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * GlobalExceptionHandler class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Executes handleEntityNotFoundException.
     *
     * @param ex input argument consumed by handleEntityNotFoundException.
     * @param request input argument consumed by handleEntityNotFoundException.
     * @return computed Object>> result returned by handleEntityNotFoundException.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), ex);
    }

    /**
     * Executes handleIllegalArgumentException.
     *
     * @param ex input argument consumed by handleIllegalArgumentException.
     * @param request input argument consumed by handleIllegalArgumentException.
     * @return computed Object>> result returned by handleIllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), ex);
    }

    /**
     * Executes handleIllegalStateException.
     *
     * @param ex input argument consumed by handleIllegalStateException.
     * @param request input argument consumed by handleIllegalStateException.
     * @return computed Object>> result returned by handleIllegalStateException.
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), ex);
    }

    /**
     * Executes handleDataIntegrityViolationException.
     *
     * @param ex input argument consumed by handleDataIntegrityViolationException.
     * @param request input argument consumed by handleDataIntegrityViolationException.
     * @return computed Object>> result returned by handleDataIntegrityViolationException.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Database constraint violation",
            request.getRequestURI(),
            ex);
    }

    /**
     * Executes handleRequestBindingException.
     *
     * @param ex input argument consumed by handleRequestBindingException.
     * @param request input argument consumed by handleRequestBindingException.
     * @return computed Object>> result returned by handleRequestBindingException.
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleRequestBindingException(
            Exception ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters", request.getRequestURI(), ex);
    }

    /**
     * Executes handleAllExceptions.
     *
     * @param ex input argument consumed by handleAllExceptions.
     * @param request input argument consumed by handleAllExceptions.
     * @return computed Object>> result returned by handleAllExceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), ex);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            Exception exception) {
        String businessCode = BusinessCodeResolver.resolveErrorCode(status, path, message, exception);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", businessCode);
        body.put("message", message);
        body.put("path", path);

        HttpHeaders headers = new HttpHeaders();
        headers.set(BusinessCodeResolver.HEADER_NAME, businessCode);
        return ResponseEntity.status(status).headers(headers).body(body);
    }
}
