package dev.d76.spring.exception.autoconfigure;

import dev.d76.spring.exception.BusinessException;
import dev.d76.spring.exception.CommonErrorCode;
import dev.d76.spring.exception.ErrorCode;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponse;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponseCustomizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class GlobalExceptionHandler {

    private final List<ApiErrorResponseCustomizer> customizers;

    public GlobalExceptionHandler(List<ApiErrorResponseCustomizer> customizers) {
        this.customizers = List.copyOf(customizers);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        ErrorCode errorCode = ex.getErrorCode();
        var responseBuilder = ApiErrorResponse.builderFrom(errorCode, ex.getMessage(), request);

        applyCustomizers(responseBuilder, ex, request, response);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(responseBuilder.build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        List<ApiErrorResponse.ApiFieldError> fieldErrors = ex
                .getBindingResult().getFieldErrors()
                .stream().map(fieldError ->
                        new ApiErrorResponse.ApiFieldError(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                )
                .toList();

        var errorCode = CommonErrorCode.VALIDATION_FAILED;

        var responseBuilder = ApiErrorResponse
                .builderFrom(errorCode, request)
                .errors(fieldErrors);

        applyCustomizers(responseBuilder, ex, request, response);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(responseBuilder.build());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        var errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        var responseBuilder = ApiErrorResponse.builderFrom(errorCode, request);

        applyCustomizers(responseBuilder, ex, request, response);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(responseBuilder.build());
    }

    private void applyCustomizers(ApiErrorResponse.Builder builder, Exception ex, HttpServletRequest request, HttpServletResponse response) {
        for (var customizer : customizers) {
            customizer.customize(builder, ex, request, response);
        }
    }
}
