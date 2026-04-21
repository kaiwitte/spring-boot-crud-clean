package com.witteconsulting.cruddemo.mapper;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapperTest {
    @Test
    void shouldMapDtoToEntity() {
        // Given
        RoomRequestDto dto = new RoomRequestDto();
        dto.setName("Conference Room");

        // When
        RoomEntity entity = RoomMapper.INSTANCE.dtoToEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Conference Room");
        assertThat(entity.getId()).isNull();
    }

    @Test
    void shouldMapEntityToDto() {
        // Given
        UUID id = UUID.randomUUID();
        RoomEntity entity = RoomEntity.builder().id(id).name("Board Room").build();

        // When
        RoomResponseDto dto = RoomMapper.INSTANCE.entityToDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Board Room");
    }
}
