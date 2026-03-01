package dev.d76.spring.exception.autoconfigure.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.d76.spring.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(Instant timestamp, String errorCode, int httpStatusCode, String message, String path,
                               List<ApiFieldError> errors, Map<String, Object> extensions) {

    public ApiErrorResponse {
        errors = errors != null ? List.copyOf(errors) : List.of();
        extensions = extensions != null ? Map.copyOf(extensions) : Map.of();
    }

    public static Builder builderFrom(ErrorCode code, HttpServletRequest request) {
        return ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .errorCode(code.getErrorCode())
                .httpStatusCode(code.getHttpStatus())
                .message(code.defaultMessage())
                .path(request.getRequestURI());
    }

    public static Builder builderFrom(ErrorCode code, String message, HttpServletRequest request) {
        return ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .errorCode(code.getErrorCode())
                .httpStatusCode(code.getHttpStatus())
                .message(message)
                .path(request.getRequestURI());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> extensions() {
        return extensions;
    }

    public record ApiFieldError(String field, String message) {
    }

    public final static class Builder {
        private final Map<String, Object> extensions = new LinkedHashMap<>();
        private Instant timestamp;
        private int httpStatusCode;
        private String errorCode;
        private String message;
        private String path;
        private List<ApiFieldError> errors;

        private Builder() {
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder errors(List<ApiFieldError> errors) {
            this.errors = errors;
            return this;
        }

        public Builder extension(String key, Object value) {
            Objects.requireNonNull(key, "extension key cannot be null");
            Objects.requireNonNull(value, "extension value cannot be null");
            extensions.put(key, value);
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, errorCode, httpStatusCode, message, path, errors, extensions);
        }
    }
}