package gov.rw.javane.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String message,
        List<FieldError> errors,
        Instant timestamp
) {
    public record FieldError(String field, String message) {}

    public static ApiErrorResponse of(String message, List<FieldError> errors) {
        return new ApiErrorResponse(message, errors, Instant.now());
    }

    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(message, null, Instant.now());
    }
}

