package gov.rw.javane.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(message, data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(message, null, Instant.now());
    }
}

