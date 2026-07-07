package com.witteconsulting.cruddemo;

import com.witteconsulting.cruddemo.model.ListRoom200ResponseDto;
import com.witteconsulting.cruddemo.model.PaginationDto;
import com.witteconsulting.cruddemo.model.RoomRequestDto;
import com.witteconsulting.cruddemo.model.RoomResponseDto;
import com.witteconsulting.cruddemo.model.SortDto;
import com.witteconsulting.cruddemo.repository.RoomRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;

class RoomIT extends AbstractCrudIT<RoomRequestDto, RoomResponseDto, ListRoom200ResponseDto> {

    private final RoomRepository roomRepository;

    @Autowired
    RoomIT(final RoomRepository roomRepository) {
        super(RoomResponseDto.class, ListRoom200ResponseDto.class, "/rooms");
        this.roomRepository = roomRepository;
    }

    @Override
    RoomRequestDto templateCreateValidRequest(final String methodName, final int enumerator) {
        return new RoomRequestDto().name("%s-%d".formatted(methodName, enumerator));
    }

    @Override
    Collection<InvalidRequestTestParameter<RoomRequestDto>> templateInvalidRequestExamples() {
        return List.of(
                new InvalidRequestTestParameter<>("null name", new RoomRequestDto().name(null), new String[] {"name"}),
                new InvalidRequestTestParameter<>("empty name", new RoomRequestDto().name(""), new String[] {"name"}));
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
        roomRepository.deleteAll();
    }

    @Override
    String templateSortField() {
        return "name";
    }

    @Override
    PaginationDto templateExtractPagination(final ListRoom200ResponseDto listResponse) {
        return listResponse.getPagination();
    }

    @Override
    SortDto templateExtractSort(final ListRoom200ResponseDto listResponse) {
        return listResponse.getSort();
    }

    @Override
    Comparator<RoomResponseDto> templateSortComparator() {
        return Comparator.comparing(RoomResponseDto::getName);
    }

    @Override
    Collection<FilterTestParameter<RoomRequestDto>> templateFilterExamples() {
        final RoomRequestDto findThis = new RoomRequestDto().name("templateFilterExamples-findThis");
        final RoomRequestDto notToBeFound = new RoomRequestDto().name("templateFilterExamples-notToBeFound");
        return List.of(
                new FilterTestParameter<>("filter by exact name", RoomRequestDto::getName, findThis, notToBeFound),
                new FilterTestParameter<>("filter by name fragment", room -> "findThis", findThis, notToBeFound));
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
}
