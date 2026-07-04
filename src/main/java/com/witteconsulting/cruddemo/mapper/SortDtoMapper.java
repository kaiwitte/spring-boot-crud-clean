package com.witteconsulting.cruddemo.mapper;

import com.witteconsulting.cruddemo.model.SortDto;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@UtilityClass
public class SortDtoMapper {
    public static SortDto map(final Page<?> pagedResult) {
        final Optional<Sort.Order> optionalSort = pagedResult.getSort().get().findAny();
        final SortDto.DirectionEnum direction = optionalSort
                .map(Sort.Order::getDirection)
                .map(dir -> dir.equals(Sort.Direction.ASC) ? SortDto.DirectionEnum.ASC : SortDto.DirectionEnum.DESC)
                .orElse(null);
        final String field = optionalSort.map(Sort.Order::getProperty).orElse(null);
        return new SortDto().direction(direction).field(field);
    }
}
