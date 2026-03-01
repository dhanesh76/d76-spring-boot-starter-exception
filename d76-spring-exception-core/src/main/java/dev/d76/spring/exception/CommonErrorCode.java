package dev.d76.spring.exception;

public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    CONFLICT(409, "Conflict occurred"),
    VALIDATION_FAILED(400, "Validation failed"),
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable");

    private final int status;
    private final String message;

    CommonErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public int getHttpStatus() {
        return status;
    }

    @Override
    public String defaultMessage() {
        return message;
    }
}