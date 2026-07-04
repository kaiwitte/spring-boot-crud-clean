package com.witteconsulting.cruddemo.mapper;

import com.witteconsulting.cruddemo.model.PaginationDto;
import java.math.BigDecimal;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class PaginationDtoMapper {
    public static PaginationDto map(final Page<?> pagedResult) {
        return new PaginationDto()
                .total(BigDecimal.valueOf(pagedResult.getTotalElements()))
                .index(BigDecimal.valueOf(pagedResult.getNumber()))
                .size(BigDecimal.valueOf(pagedResult.getSize()))
                .hasNext(pagedResult.hasNext())
                .hasPrevious(pagedResult.hasPrevious());
    }
}
