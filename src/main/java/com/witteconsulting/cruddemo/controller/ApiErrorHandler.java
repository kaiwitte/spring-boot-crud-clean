package com.witteconsulting.cruddemo.controller;

import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.INTERNAL_SERVER_ERROR;
import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.VALIDATION_ERROR;

import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ApiErrorHandler extends ResponseEntityExceptionHandler {
    /**
     * Create error message for bean validation failures.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final @Nonnull HttpHeaders headers,
            final @Nonnull HttpStatusCode status,
            final @Nonnull WebRequest request) {
        final Map<String, String> errorMap = new HashMap<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMap.put(error.getField(), error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errorMap.put(error.getObjectName(), error.getDefaultMessage());
        }

        final var errorBody = new ApplicationErrorResponseDto()
                .errorType(VALIDATION_ERROR)
                .message("invalid fields")
                .fieldErrors(errorMap);
        return handleExceptionInternal(ex, errorBody, headers, HttpStatusCode.valueOf(400), request);
    }

    /**
     * Handle sort field errors as 400 and other causes as 500.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApplicationErrorResponseDto> sortFieldError(
            final PropertyReferenceException e, final HttpServletRequest request) {
        if (!isSortParameterToken(request, e.getPropertyName())) {
            return handleException(e, request);
        }
        log.info(
                "rejected sort by unknown field or direction '{}' (request: {}?{})",
                e.getPropertyName(),
                request.getRequestURL(),
                request.getQueryString());
        final ApplicationErrorResponseDto errorBody = new ApplicationErrorResponseDto()
                .errorType(VALIDATION_ERROR)
                .message("sort or direction field unknown")
                .fieldErrors(Map.of("sort", "must be a field of this service, followed by ',ASC' or ',DESC'"));
        return ResponseEntity.badRequest().body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorResponseDto> handleException(
            final Exception e, final HttpServletRequest request) {
        log.error("unhandled exception (request: {}?{})", request.getRequestURL(), request.getQueryString(), e);
        final ApplicationErrorResponseDto errorBody = new ApplicationErrorResponseDto()
                .errorType(INTERNAL_SERVER_ERROR)
                .message("an unexpected error occurred");
        return ResponseEntity.internalServerError().body(errorBody);
    }

    private static boolean isSortParameterToken(final HttpServletRequest request, final String propertyName) {
        return Optional.ofNullable(request.getParameterValues("sort")).stream()
                .flatMap(Arrays::stream)
                .flatMap(value -> Arrays.stream(value.split(",")))
                .anyMatch(propertyName::equals);
    }
}
