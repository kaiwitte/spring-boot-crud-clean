package com.witteconsulting.cruddemo;

import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.model.ListRoom200ResponseDto;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateRoom() {
        final RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName("Conference Room B");

        final ResponseEntity<RoomResponseDto> response =
                restTemplate.postForEntity("/rooms", roomRequest, RoomResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Conference Room B");
    }

    @Test
    void shouldFailToCreateRoomWithoutName() {
        final RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName(null); // Name is required

        final ResponseEntity<RoomResponseDto> response =
                restTemplate.postForEntity("/rooms", roomRequest, RoomResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldPersistRoomAndRetrieveById() {
        // Create a room
        final RoomRequestDto roomRequest = new RoomRequestDto();
        roomRequest.setName("Persistent Room");

        final ResponseEntity<RoomResponseDto> createResponse =
                restTemplate.postForEntity("/rooms", roomRequest, RoomResponseDto.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        final String roomId = createResponse.getBody().getId().toString();

        // Retrieve the room by ID to verify persistence
        final ResponseEntity<RoomResponseDto> getResponse =
                restTemplate.getForEntity("/rooms/" + roomId, RoomResponseDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId().toString()).isEqualTo(roomId);
        assertThat(getResponse.getBody().getName()).isEqualTo("Persistent Room");
    }

    @Test
    void shouldReturn404ForNonExistentRoom() {
        final UUID nonExistentRoomId = UUID.randomUUID();

        final ResponseEntity<RoomResponseDto> response =
                restTemplate.getForEntity("/rooms/" + nonExistentRoomId, RoomResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldListRooms() {
        // given
        final RoomRequestDto firstRoomRequest = new RoomRequestDto();
        firstRoomRequest.setName("Listing Room Alpha");
        final RoomRequestDto secondRoomRequest = new RoomRequestDto();
        secondRoomRequest.setName("Listing Room Beta");

        final ResponseEntity<RoomResponseDto> firstCreateResponse =
                restTemplate.postForEntity("/rooms", firstRoomRequest, RoomResponseDto.class);
        final ResponseEntity<RoomResponseDto> secondCreateResponse =
                restTemplate.postForEntity("/rooms", secondRoomRequest, RoomResponseDto.class);

        assertThat(firstCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(firstCreateResponse.getBody()).isNotNull();
        assertThat(secondCreateResponse.getBody()).isNotNull();

        // when
        final ResponseEntity<ListRoom200ResponseDto> listResponse =
                restTemplate.getForEntity("/rooms", ListRoom200ResponseDto.class);

        // then
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getResults())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsAll(List.of(firstCreateResponse.getBody(), secondCreateResponse.getBody()));
    }
}
