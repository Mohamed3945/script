package st.tt.script_back.exceptions;

import java.util.Locale;

import org.springframework.http.HttpStatus;

/**
 * Resolves stable business response codes used by the frontend feedback layer.
 * <p>
 * Codes are derived from HTTP method/path for success responses and from status/cause patterns for errors.
 * This keeps user-facing messages decoupled from raw exception text.
 */
public final class BusinessCodeResolver {

    public static final String HEADER_NAME = "X-Business-Code";

    private BusinessCodeResolver() {
    }

    /**
     * Resolves a success business code from request method, path, and HTTP status.
     *
     * @param method HTTP method.
     * @param path normalized request path.
     * @param status HTTP response status code.
     * @return success business code, or {@code null} when response is not in 2xx range.
     */
    public static String resolveSuccessCode(String method, String path, int status) {
        if (status < 200 || status >= 300) {
            return null;
        }

        String normalizedMethod = method == null ? "" : method.toUpperCase(Locale.ROOT);
        String normalizedPath = path == null ? "" : path;

        if (normalizedPath.contains("/recipes/")) {
            if ("POST".equals(normalizedMethod)) {
                return "RECIPE_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "RECIPE_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "RECIPE_DELETED";
            }
        }

        if (normalizedPath.contains("/steps/")) {
            if ("POST".equals(normalizedMethod)) {
                return "STEP_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "STEP_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "STEP_DELETED";
            }
        }

        if (normalizedPath.contains("/step-parameters/")) {
            if ("POST".equals(normalizedMethod)) {
                return "STEP_PARAMETER_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "STEP_PARAMETER_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "STEP_PARAMETER_DELETED";
            }
        }

        if (normalizedPath.contains("/parameter-definitions/")) {
            if ("POST".equals(normalizedMethod)) {
                return "PARAMETER_DEFINITION_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "PARAMETER_DEFINITION_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "PARAMETER_DEFINITION_DELETED";
            }
        }

        if (normalizedPath.contains("/parameter-options/")) {
            if ("POST".equals(normalizedMethod)) {
                return "PARAMETER_OPTION_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "PARAMETER_OPTION_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "PARAMETER_OPTION_DELETED";
            }
        }

        if (normalizedPath.contains("/parameter-dependency-rules")) {
            if ("POST".equals(normalizedMethod)) {
                return "PARAMETER_DEPENDENCY_RULE_CREATED";
            }
            if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
                return "PARAMETER_DEPENDENCY_RULE_UPDATED";
            }
            if ("DELETE".equals(normalizedMethod)) {
                return "PARAMETER_DEPENDENCY_RULE_DELETED";
            }
        }

        if ("POST".equals(normalizedMethod)) {
            return "RESOURCE_CREATED";
        }
        if ("PUT".equals(normalizedMethod) || "PATCH".equals(normalizedMethod)) {
            return "RESOURCE_UPDATED";
        }
        if ("DELETE".equals(normalizedMethod)) {
            return "RESOURCE_DELETED";
        }
        return "REQUEST_SUCCEEDED";
    }

    /**
     * Resolves an error business code from HTTP status and known domain/integrity failure signatures.
     * <p>
     * The method inspects request path, exception message, and chained causes to identify precise error families
     * such as resource-in-use, dependency rule conflicts, or derivative constraints.
     *
     * @param status HTTP status associated with the error.
     * @param path request path.
     * @param message top-level error message.
     * @param throwable original throwable used to inspect chained causes.
     * @return normalized error business code.
     */
    public static String resolveErrorCode(HttpStatus status, String path, String message, Throwable throwable) {
        String normalizedPath = path == null ? "" : path;
        String normalizedMessage = message == null ? "" : message.toLowerCase(Locale.ROOT);
        String normalizedCause = flattenThrowableMessage(throwable).toLowerCase(Locale.ROOT);

        if (normalizedPath.contains("/recipes/")
                && (normalizedCause.contains("fk_recipe_parent_recipe")
                        || normalizedCause.contains("cannot delete or update a parent row")
                        || normalizedMessage.contains("derive"))) {
            return "RECIPE_HAS_DERIVATIVES";
        }

        if (normalizedPath.contains("/parameter-options/")
                && (normalizedCause.contains("fk_step_parameter_selected_option")
                        || normalizedCause.contains("selected_option_id"))) {
            return "PARAMETER_OPTION_IN_USE";
        }

        if (normalizedPath.contains("/parameter-definitions/")
            && (normalizedMessage.contains("this parameter is used")
                || normalizedMessage.contains("an option of this parameter")
                        || normalizedCause.contains("fk_step_parameter_selected_option")
                        || normalizedCause.contains("selected_option_id"))) {
            return "PARAMETER_DEFINITION_IN_USE";
        }

        if (normalizedPath.contains("/parameter-dependency-rules")
            && (normalizedCause.contains("uq_pdr_source_trigger_target")
                || normalizedCause.contains("a dependency rule cannot target its own source definition")
                || normalizedCause.contains("trigger option must belong to source definition")
                || normalizedCause.contains("required source activation option must belong to source definition")
                || normalizedMessage.contains("sourcedefinitionid, triggeroptionid and targetdefinitionid are required"))) {
            return "PARAMETER_DEPENDENCY_RULE_CONFLICT";
        }

        if (status == HttpStatus.NOT_FOUND) {
            return "RESOURCE_NOT_FOUND";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return "VALIDATION_ERROR";
        }
        if (status == HttpStatus.CONFLICT) {
            return "BUSINESS_CONFLICT";
        }
        return "INTERNAL_ERROR";
    }

    /**
     * Flattens throwable cause chain messages into a single searchable string.
     *
     * @param throwable root throwable.
     * @return concatenated messages from all causes separated by {@code |}.
     */
    private static String flattenThrowableMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Throwable cursor = throwable;

        while (cursor != null) {
            if (cursor.getMessage() != null && !cursor.getMessage().isBlank()) {
                if (builder.length() > 0) {
                    builder.append(" | ");
                }
                builder.append(cursor.getMessage());
            }
            cursor = cursor.getCause();
        }

        return builder.toString();
    }
}
