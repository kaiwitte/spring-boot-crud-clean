package com.witteconsulting.cruddemo.mapper;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    RoomEntity dtoToEntity(RoomRequestDto dto);

    RoomResponseDto entityToDto(RoomEntity entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(RoomRequestDto dto, @MappingTarget RoomEntity entity);
}
