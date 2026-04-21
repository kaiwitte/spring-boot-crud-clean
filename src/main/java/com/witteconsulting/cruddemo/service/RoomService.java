package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.RoomsApiDelegate;
import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.mapper.RoomMapper;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import com.witteconsulting.cruddemo.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService implements RoomsApiDelegate {

    private final RoomRepository roomRepository;

    @Override
    public ResponseEntity<RoomResponseDto> newRoom(final RoomRequestDto roomRequestDto) {
        final RoomEntity entity = RoomMapper.INSTANCE.dtoToEntity(roomRequestDto);

        final RoomEntity savedEntity = roomRepository.save(entity);

        final RoomResponseDto response = RoomMapper.INSTANCE.entityToDto(savedEntity);

        return ResponseEntity.created(URI.create("/rooms/" + savedEntity.getId()))
                .body(response);
    }

    @Override
    public ResponseEntity<RoomResponseDto> getRoom(final UUID roomId) {
        return roomRepository
                .findById(roomId)
                .map(RoomMapper.INSTANCE::entityToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
