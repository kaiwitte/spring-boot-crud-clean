package com.witteconsulting.cruddemo;

import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.model.HelloResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class HelloServiceIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRespondWithGreeting() {
        final ResponseEntity<HelloResponseDto> response = restTemplate.getForEntity("/hello", HelloResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isNotNull();
    }
}
