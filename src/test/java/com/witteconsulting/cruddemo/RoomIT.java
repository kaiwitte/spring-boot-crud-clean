package com.witteconsulting.cruddemo;

import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateRoom() {
        final RoomRequestDto roomRequest = new RoomRequestDto();
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

    @Test
    void shouldFailToCreateRoomWithoutName() {
        final RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName(null); // Name is required

        final ResponseEntity<RoomResponseDto> response = restTemplate.postForEntity(
                "/rooms",
                roomRequest,
                RoomResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldPersistRoomAndRetrieveById() {
        // Create a room
        final RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName("Persistent Room");

        final ResponseEntity<RoomResponseDto> createResponse = restTemplate.postForEntity(
                "/rooms",
                roomRequest,
                RoomResponseDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        final String roomId = createResponse.getBody().getId().toString();

        // Retrieve the room by ID to verify persistence
        final ResponseEntity<RoomResponseDto> getResponse = restTemplate.getForEntity(
                "/rooms/" + roomId,
                RoomResponseDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId().toString()).isEqualTo(roomId);
        assertThat(getResponse.getBody().getName()).isEqualTo("Persistent Room");
    }

    @Test
    void shouldReturn404ForNonExistentRoom() {
        final UUID nonExistentRoomId = UUID.randomUUID();

        final ResponseEntity<RoomResponseDto> response = restTemplate.getForEntity(
                "/rooms/" + nonExistentRoomId,
                RoomResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}