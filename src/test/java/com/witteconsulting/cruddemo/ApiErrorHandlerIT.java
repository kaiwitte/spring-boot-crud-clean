package com.witteconsulting.cruddemo;

import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Asserts the error contract over the wire for failures that no real
 * endpoint can trigger, using the test-only ErrorHandlingTestController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApiErrorHandlerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldMapNonSortPropertyReferenceToInternalError() {
        final ResponseEntity<ApplicationErrorResponseDto> response =
                restTemplate.getForEntity("/test-errors/property-reference", ApplicationErrorResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldMapUnexpectedExceptionToGenericInternalError() {
        final ResponseEntity<ApplicationErrorResponseDto> response =
                restTemplate.getForEntity("/test-errors/runtime-exception", ApplicationErrorResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(body.getMessage()).doesNotContain("secret detail");
    }
}
