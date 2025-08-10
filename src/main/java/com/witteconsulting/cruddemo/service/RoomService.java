package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.RoomsApiDelegate;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomService implements RoomsApiDelegate {

    @Override
    public ResponseEntity<RoomResponseDto> newRoom(final RoomRequestDto roomRequestDto) {
        final RoomResponseDto response = new RoomResponseDto()
                .id(UUID.randomUUID())
                .name(roomRequestDto.getName());

        return ResponseEntity.status(201).body(response);
    }
}