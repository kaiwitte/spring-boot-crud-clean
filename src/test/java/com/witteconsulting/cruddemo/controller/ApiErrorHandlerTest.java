package com.witteconsulting.cruddemo.controller;

import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.INTERNAL_SERVER_ERROR;
import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * GENERATED_AI_CODE: This is a class that the agent wanted to generate. I would have decided against it
 * and ditched it in a review, as the most important logic is already in ApiErrorHandlerIT and AbstractCrudIT.
 * <p>
 * I usually avoid mock-based tests to assert how an HTTP request is answered, as an integration test is
 * more realistic.
 * <p>
 * One of the stronger arguments of the agent against my objection was that the PIT mutation tests would
 * be much faster with this test present, as they would realise that this test runs faster than the IT that also
 * cover the same lines. Something I would not have known, by the way, nor had the time to read into in
 * this detail.
 * <p>
 * But since this project is for discussion and learning, I'll leave it in.
 */
class ApiErrorHandlerTest {

    private final ApiErrorHandler handler = new ApiErrorHandler();

    /** Provokes a real PropertyReferenceException instead of stubbing one. */
    private static PropertyReferenceException propertyReferenceException(
            @SuppressWarnings("SameParameterValue") final String propertyName) {
        return catchThrowableOfType(
                PropertyReferenceException.class, () -> PropertyPath.from(propertyName, RoomEntity.class));
    }

    @Test
    void shouldMapToValidationErrorWhenPropertyIsASortToken() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "unknownField,ASC");

        final ResponseEntity<ApplicationErrorResponseDto> response =
                handler.sortFieldError(propertyReferenceException("unknownField"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors()).containsOnlyKeys("sort");
    }

    @Test
    void shouldMapToValidationErrorWhenPropertyIsInAnyRepeatedSortParameter() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "name,ASC");
        request.addParameter("sort", "unknownField,DESC");

        final ResponseEntity<ApplicationErrorResponseDto> response =
                handler.sortFieldError(propertyReferenceException("unknownField"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldMapToInternalErrorWhenRequestHasNoSortParameter() {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final ResponseEntity<ApplicationErrorResponseDto> response =
                handler.sortFieldError(propertyReferenceException("unknownField"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldMapToInternalErrorWhenPropertyIsNotAmongTheSortTokens() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "name,ASC");

        final ResponseEntity<ApplicationErrorResponseDto> response =
                handler.sortFieldError(propertyReferenceException("unknownField"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldMapUnexpectedExceptionToInternalErrorWithoutLeakingDetails() {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final ResponseEntity<ApplicationErrorResponseDto> response =
                handler.handleException(new IllegalStateException("secret detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(body.getMessage()).doesNotContain(ErrorHandlingTestController.DO_NOT_LEAK_SERVER_ERROR);
    }
}
