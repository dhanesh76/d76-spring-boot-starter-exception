package dev.d76.spring.exception.autoconfigure.web;


import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface ApiErrorResponseCustomizer {
    void customize(ApiErrorResponse.Builder builder, Exception ex, HttpServletRequest request);
}
