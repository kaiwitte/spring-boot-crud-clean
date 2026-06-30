package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.RoomsApiDelegate;
import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.mapper.RoomMapper;
import com.witteconsulting.cruddemo.model.ListRoom200ResponseDto;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import com.witteconsulting.cruddemo.repository.RoomRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    @Override
    public ResponseEntity<Void> deleteRoom(final UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            return ResponseEntity.notFound().build();
        }

        roomRepository.deleteById(roomId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RoomResponseDto> updateRoom(final UUID roomId, final RoomRequestDto roomRequestDto) {
        return roomRepository
                .findById(roomId)
                .map(entity -> {
                    RoomMapper.INSTANCE.updateEntityFromDto(roomRequestDto, entity);
                    final RoomEntity savedEntity = roomRepository.save(entity);
                    return RoomMapper.INSTANCE.entityToDto(savedEntity);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ListRoom200ResponseDto> listRoom() {
        final List<RoomResponseDto> result = roomRepository.findAll().stream()
                .map(RoomMapper.INSTANCE::entityToDto)
                .toList();

        return ResponseEntity.ok(new ListRoom200ResponseDto().results(result));
    }
}
