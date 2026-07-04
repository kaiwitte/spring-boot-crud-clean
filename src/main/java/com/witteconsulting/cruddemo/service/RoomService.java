package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.RoomsApiDelegate;
import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.mapper.PaginationDtoMapper;
import com.witteconsulting.cruddemo.mapper.RoomMapper;
import com.witteconsulting.cruddemo.mapper.SortDtoMapper;
import com.witteconsulting.cruddemo.model.ListRoom200ResponseDto;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import com.witteconsulting.cruddemo.repository.RoomRepository;
import com.witteconsulting.cruddemo.repository.Search;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RoomService implements RoomsApiDelegate {

    private final RoomRepository roomRepository;

    @Override
    public ResponseEntity<RoomResponseDto> newRoom(final RoomRequestDto roomRequestDto) {
        final RoomEntity entity = RoomMapper.INSTANCE.dtoToEntity(roomRequestDto);

        final RoomEntity savedEntity = roomRepository.save(entity);

        final RoomResponseDto response = RoomMapper.INSTANCE.entityToDto(savedEntity);

        final URI location = UriComponentsBuilder.fromPath("/rooms")
                .pathSegment(savedEntity.getId().toString())
                .build()
                .toUri();

        return ResponseEntity.created(location).body(response);
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
    public ResponseEntity<ListRoom200ResponseDto> listRoom(final String filter, final Pageable pageable) {
        final Page<RoomEntity> pages = roomRepository.findAll(Search.matchesSearch(filter, "name"), pageable);
        final List<RoomResponseDto> result =
                pages.stream().map(RoomMapper.INSTANCE::entityToDto).toList();

        return ResponseEntity.ok(new ListRoom200ResponseDto()
                .pagination(PaginationDtoMapper.map(pages))
                .sort(SortDtoMapper.map(pages))
                .results(result));
    }
}
