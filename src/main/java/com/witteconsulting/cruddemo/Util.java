package com.witteconsulting.cruddemo;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class Util {
    public static Pageable pageable(
            final Integer pageIndex, final Integer pageSize, final String sortDirection, final String sortField) {
        final Sort sort = createSort(sortDirection, sortField);
        if (pageIndex == null || pageSize == null) {
            return Pageable.unpaged(sort);
        }

        validatePageIndex(pageIndex);
        validatePageSize(pageSize);

        return PageRequest.of(pageIndex, pageSize, sort);
    }

    private static void validatePageIndex(final Integer pageIndex) {
        if (pageIndex < 0) {
            throw new PaginationException("pageIndex", "must be greater than or equal to 0");
        }
    }

    private static void validatePageSize(final Integer pageSize) {
        if (pageSize < 1) {
            throw new PaginationException("pageSize", "must be greater than 0");
        }
    }

    private static Sort createSort(final String sortDirection, final String sortField) {
        if (sortField == null || sortDirection == null) {
            return Sort.unsorted();
        } else {
            try {
                return Sort.by(Sort.Direction.fromString(sortDirection), sortField);
            } catch (final IllegalArgumentException e) {
                throw new PaginationException("sortDirection", "must be 'ASC' or 'DESC'");
            }
        }
    }
}
