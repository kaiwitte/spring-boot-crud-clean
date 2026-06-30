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
    RoomRequestDto createNew(final String methodName, final int enumerator) {
        return new RoomRequestDto().name("%s-%d".formatted(methodName, enumerator));
    }

    @Override
    RoomRequestDto createNewInvalid(final String methodName) {
        return new RoomRequestDto().name(null);
    }

    @Override
    String[] getFieldNamesWithInvalidValues() {
        return new String[] {"name"};
    }

    @Override
    UUID extractId(final RoomResponseDto dto) {
        return dto.getId();
    }

    @Override
    String[] getComparisonIgnoreFields() {
        return new String[] {"id"};
    }

    @Override
    List<RoomResponseDto> extractResults(final ListRoom200ResponseDto body) {
        return body.getResults();
    }

    @Override
    void deleteAll() {
        throw unsupported("deleteAll");
    }

    @Override
    String getSortField() {
        throw unsupported("getSortField");
    }

    @Override
    PaginationDto getPagination(final ListRoom200ResponseDto listResponse) {
        throw unsupported("getPagination");
    }

    @Override
    SortDto getSort(final ListRoom200ResponseDto listResponse) {
        throw unsupported("getSort");
    }

    @Override
    Comparator<RoomResponseDto> getComparator() {
        throw unsupported("getComparator");
    }

    @Override
    Stream<FilterTestParameter<RoomResponseDto>> getSearchParameters() {
        throw unsupported("getSearchParameters");
    }

    @Override
    RoomRequestDto createRequestFromResponse(final RoomResponseDto response) {
        return new RoomRequestDto().name(response.getName());
    }

    @Override
    Stream<EditTestParameter<RoomRequestDto>> getModifications() {
        return Stream.of(
                new EditTestParameter<>("change name", dto -> dto.setName("%s-updated".formatted(dto.getName()))));
    }

    private AssertionError unsupported(final String methodName) {
        return new AssertionError(methodName + " is not implemented for the current RoomIT subset.");
    }
}
