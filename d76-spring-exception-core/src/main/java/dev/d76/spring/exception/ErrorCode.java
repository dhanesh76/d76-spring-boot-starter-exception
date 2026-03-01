package dev.d76.spring.exception;

public interface ErrorCode {

    int getHttpStatus();

    String defaultMessage();

    default String getErrorCode() {
        return this instanceof Enum<?> e ? e.name() : this.getClass().getSimpleName();
    }
}
