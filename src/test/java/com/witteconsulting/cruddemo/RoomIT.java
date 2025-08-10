package com.witteconsulting.cruddemo;

import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateRoom() {
        RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName("Conference Room A");

        final ResponseEntity<RoomResponseDto> response = restTemplate.postForEntity(
                "/rooms",
                roomRequest,
                RoomResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Conference Room A");
    }
}