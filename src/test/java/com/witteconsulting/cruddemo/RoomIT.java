package com.witteconsulting.cruddemo;

import com.witteconsulting.cruddemo.model.ListRoom200ResponseDto;
import com.witteconsulting.cruddemo.model.PaginationDto;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import com.witteconsulting.cruddemo.model.SortDto;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

class RoomIT extends AbstractCrudIT<RoomRequestDto, RoomResponseDto, ListRoom200ResponseDto> {

    RoomIT() {
        super(RoomResponseDto.class, ListRoom200ResponseDto.class, "/rooms");
    }

    @Override
    RoomRequestDto templateCreateValidRequest(final String methodName, final int enumerator) {
        return new RoomRequestDto().name("%s-%d".formatted(methodName, enumerator));
    }

    @Override
    RoomRequestDto templateCreateInvalidRequest(final String methodName) {
        return new RoomRequestDto().name(null);
    }

    @Override
    String[] templateInvalidRequestFieldNames() {
        return new String[] {"name"};
    }

    @Override
    UUID templateExtractId(final RoomResponseDto dto) {
        return dto.getId();
    }

    @Override
    String[] templateComparisonIgnoredFields() {
        return new String[] {"id"};
    }

    @Override
    List<RoomResponseDto> templateExtractResults(final ListRoom200ResponseDto body) {
        return body.getResults();
    }

    @Override
    void templateDeleteAllExisting() {
        throw unsupported("templateDeleteAllExisting");
    }

    @Override
    String templateSortField() {
        throw unsupported("templateSortField");
    }

    @Override
    PaginationDto templateExtractPagination(final ListRoom200ResponseDto listResponse) {
        throw unsupported("templateExtractPagination");
    }

    @Override
    SortDto templateExtractSort(final ListRoom200ResponseDto listResponse) {
        throw unsupported("templateExtractSort");
    }

    @Override
    Comparator<RoomResponseDto> templateSortComparator() {
        throw unsupported("templateSortComparator");
    }

    @Override
    Stream<FilterTestParameter<RoomResponseDto>> templateFilterExamples() {
        throw unsupported("templateFilterExamples");
    }

    @Override
    RoomRequestDto templateCreateRequestFromResponse(final RoomResponseDto response) {
        return new RoomRequestDto().name(response.getName());
    }

    @Override
    Stream<EditTestParameter<RoomRequestDto>> templateUpdateModifications() {
        return Stream.of(
                new EditTestParameter<>("change name", dto -> dto.setName("%s-updated".formatted(dto.getName()))));
    }

    private AssertionError unsupported(final String methodName) {
        return new AssertionError(methodName + " is not implemented for the current RoomIT subset.");
    }
}
