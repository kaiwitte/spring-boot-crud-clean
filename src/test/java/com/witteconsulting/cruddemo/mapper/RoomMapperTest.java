package com.witteconsulting.cruddemo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomMapperTest {
    @Test
    void shouldMapDtoToEntity() {
        // Given
        final RoomRequestDto dto = new RoomRequestDto();
        dto.setName("Conference Room");

        // When
        final RoomEntity entity = RoomMapper.INSTANCE.dtoToEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Conference Room");
        assertThat(entity.getId()).isNull();
    }

    @Test
    void shouldMapEntityToDto() {
        // Given
        final UUID id = UUID.randomUUID();
        final RoomEntity entity = RoomEntity.builder().id(id).name("Board Room").build();

        // When
        final RoomResponseDto dto = RoomMapper.INSTANCE.entityToDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Board Room");
    }

    @Test
    void shouldUpdateEntityFromDto() {
        // Given
        final UUID id = UUID.randomUUID();
        final RoomEntity originalEntity =
                RoomEntity.builder().id(id).name("Original Room").build();
        final RoomRequestDto updateRequestDto = new RoomRequestDto();
        updateRequestDto.setName("Updated Room");

        // When
        RoomMapper.INSTANCE.updateEntityFromDto(updateRequestDto, originalEntity);

        // Then
        assertThat(originalEntity.getName()).isEqualTo("Updated Room");
    }

    @Test
    void shouldPreserveEntityIdWhenUpdatingFromDto() {
        // Given
        final UUID existingId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();
        final RoomEntity entity =
                RoomEntity.builder().id(existingId).name("Original Room").build();
        final RoomRequestDto updateRequestDto = new RoomRequestDto();
        updateRequestDto.setId(requestId);
        updateRequestDto.setName("Updated Room");

        // When
        RoomMapper.INSTANCE.updateEntityFromDto(updateRequestDto, entity);

        // Then
        assertThat(entity.getId()).isEqualTo(existingId);
    }
}
