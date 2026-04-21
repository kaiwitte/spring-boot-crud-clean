package com.witteconsulting.cruddemo.mapper;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);

    RoomEntity dtoToEntity(RoomRequestDto dto);

    RoomResponseDto entityToDto(RoomEntity entity);
}
