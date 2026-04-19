package dev.d76.spring.exception.autoconfigure.web;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface ApiErrorResponseCustomizer {
    void customize(ApiErrorResponse.Builder builder,
                   Exception ex,
                   HttpServletRequest request,
                   HttpServletResponse response
    );
}
