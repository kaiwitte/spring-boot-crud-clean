package com.witteconsulting.cruddemo.controller;

import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.VALIDATION_ERROR;

import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
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
}
