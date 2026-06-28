package com.witteconsulting.cruddemo;

import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.CodeEnum.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import com.witteconsulting.cruddemo.model.PaginationDto;
import com.witteconsulting.cruddemo.model.SortDto;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * An abstract base class for general CRUD operations against a REST API.
 * <p>
 * Subclasses need to implement abstract methods to provide all information
 * that is specific to the type of entity.
 *
 * @param <TRequest>      the Dto class
 * @param <TListResponse> the Dto-List-class
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
abstract class AbstractCrudIT<TRequest, TResponse, TListResponse> {
    private final Class<TResponse> responseClass;
    private final Class<TListResponse> listClass;
    private final String endpoint;
    private final String idEndpoint;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * This parameter is used for tests related to search.
     */
    record FilterTestParameter<TResponse>(
            String name, Function<TResponse, String> searchTerm, TResponse expectedFound, TResponse expectedNotFound) {}

    /**
     * This parameter defines a modification to existing
     * data. Tests use it to verify that the modification works.
     */
    record EditTestParameter<TRequest>(String name, Consumer<TRequest> modification) {}

    protected AbstractCrudIT(
            final Class<TResponse> responseClass, final Class<TListResponse> listClass, final String endpoint) {
        this.responseClass = responseClass;
        this.listClass = listClass;
        this.endpoint = endpoint;
        this.idEndpoint = endpoint + "/{id}";
    }

    /**
     * Define how to create a new instance of the request DTO. Different enumerator
     * values should produce different instances (not == and not equal).
     * @param methodName the calling method; also encouraged to use in the creation
     * @param enumerator a number >= 0
     * @return a valid request DTO
     */
    abstract TRequest createNew(final String methodName, final int enumerator);

    abstract TRequest createNewInvalid(@SuppressWarnings("SameParameterValue") final String methodName);

    abstract String[] getInvalidFields();

    abstract UUID extractId(TResponse dto);

    abstract String[] getComparisonIgnoreFields();

    abstract List<TResponse> extractResults(final TListResponse body);

    abstract void deleteAll();

    abstract String getSortField();

    abstract PaginationDto getPagination(final TListResponse listResponse);

    abstract SortDto getSort(final TListResponse listResponse);

    abstract Comparator<TResponse> getComparator();

    /**
     * Search parameters for {@link #shouldFilterBySearchStringFactory()}
     */
    abstract Stream<FilterTestParameter<TResponse>> getSearchParameters();

    /**
     * Define how to create a new request from a received response in order
     * to do an update (PUT).
     */
    abstract TRequest createRequestFromResponse(TResponse response);

    /**
     * Modifications to test for {@link #shouldUpdateFactory()}.
     * Implement changes to a DTO here to let the test verify that those
     * are reflected in the response and in the next read.
     */
    Stream<EditTestParameter<TRequest>> getModifications() {
        // should override!
        return Stream.of(
                // noop
                new EditTestParameter<>("no change (override method?)", dto -> {}));
    }

    /**
     * Check that an ID has been generated. For check in nested classes, override.
     */
    void checkGeneratedIds(final TResponse responseDto) {
        assertThat(extractId(responseDto)).isNotNull();
    }

    @Test
    void shouldCreateNew() {
        // given
        final TRequest givenDto = createNew("shouldCreateNew", 1);

        // when
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);

        // then
        final TResponse createBody = createResponse.getBody();
        assertThat(createBody).isNotNull();
        final String expectedLocation =
                // example: "http://localhost:%d/api/v1/example/%s"
                endpoint.replace("{port}", "%d").concat("/%s").formatted(extractId(createBody));
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // todo: work with relative URI
        assertThat(createResponse.getHeaders().get("Location")).containsExactly(expectedLocation);
        assertThat(createBody)
                .usingRecursiveComparison()
                .ignoringFields(getComparisonIgnoreFields())
                .isEqualTo(givenDto);

        // retrieve newly created object
        final ResponseEntity<TResponse> getResponse = restTemplate.getForEntity(expectedLocation, responseClass);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TResponse findBody = getResponse.getBody();
        assertThat(findBody).isNotNull();
        checkGeneratedIds(findBody);
        assertThat(findBody)
                .usingRecursiveComparison()
                .ignoringFields(getComparisonIgnoreFields())
                .isEqualTo(givenDto);
    }

    @Test
    void shouldDeleteExisting() {
        // given
        final TRequest givenDto = createNew("shouldDeleteExisting", 1);
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when
        final URI deleteUri = createResponse.getHeaders().getLocation();
        restTemplate.delete(deleteUri);

        // then
        // verify the object is gone
        final ResponseEntity<String> getResponse = restTemplate.getForEntity(deleteUri, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRespondBadRequestForNewInvalid() {
        // given
        final TRequest givenRequest = createNewInvalid("shouldRespondBadRequestForNewInvalid");

        // when
        final ResponseEntity<ApplicationErrorResponseDto> response =
                restTemplate.postForEntity(endpoint, givenRequest, ApplicationErrorResponseDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        softly.assertThat(body.getCode()).isEqualTo(VALIDATION_ERROR);
        softly.assertThat(body.getFieldErrors().keySet()).containsExactlyInAnyOrder(getInvalidFields());
        softly.assertAll();
    }

    @Test
    void shouldListAllWhenUnpaginated() {
        // given
        final TRequest given1 = createNew("shouldListAllWhenUnpaginated", 1);
        final TRequest given2 = createNew("shouldListAllWhenUnpaginated", 2);
        final ResponseEntity<TResponse> createResponse1 = restTemplate.postForEntity(endpoint, given1, responseClass);
        final ResponseEntity<TResponse> createResponse2 = restTemplate.postForEntity(endpoint, given2, responseClass);
        final TResponse created1 = createResponse1.getBody();
        final TResponse created2 = createResponse2.getBody();
        assertThat(created1).isNotNull();
        assertThat(created2).isNotNull();

        // when
        final ResponseEntity<TListResponse> listResponse = restTemplate.getForEntity(endpoint, listClass);

        // then
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TListResponse body = listResponse.getBody();
        assertThat(body).isNotNull();
        final List<TResponse> results = extractResults(body);
        assertThat(results)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(getComparisonIgnoreFields())
                .isNotNull()
                .containsAll(List.of(created1, created2));
    }

    /**
     * Stores 10 Items, then fetches them in 4 pages of 3, and verifies the results.
     */
    @ParameterizedTest
    @CsvSource({"ASC", "DESC"})
    void shouldListAllPaginatedAndSorted(final String sortDirection) {
        // given
        deleteAll();
        final List<TResponse> givenResponses = new ArrayList<>();
        final ArrayList<Object> foundDtos = new ArrayList<>();
        final int numGivenDtos = 10;
        final int pageSize = 3;
        final int numExpectedDtosLastPage = 1;
        final int numExpectedPages = 4;
        final String sortField = getSortField();
        for (int i = 0; i < numGivenDtos; i++) {
            final TRequest given = createNew("shouldListAllPaginatedAndSorted", i);
            final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, given, responseClass);
            givenResponses.add(Objects.requireNonNull(createResponse.getBody()));
        }

        // when / then
        for (int pageIndex = 0; pageIndex < numExpectedPages; pageIndex++) {
            log.info("Fetching pageIndex {}", pageIndex);
            final ResponseEntity<TListResponse> responsePage = restTemplate.getForEntity(
                    endpoint + "?" + "pageIndex={pageIndex}&pageSize={pageSize}"
                            + "&sortDirection={sortDirection}&sortField={sortField}",
                    listClass,
                    pageIndex,
                    pageSize,
                    sortDirection,
                    sortField);
            assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);

            final TListResponse listResponse = responsePage.getBody();
            assertThat(listResponse).isNotNull();

            final PaginationDto pagination = getPagination(listResponse);
            assertThat(pagination).isNotNull();
            assertThat(pagination.getTotal()).isEqualTo(BigDecimal.valueOf(numGivenDtos));
            assertThat(pagination.getIndex()).isEqualTo(BigDecimal.valueOf(pageIndex));
            assertThat(pagination.getHasNext()).isEqualTo(pageIndex != 3);
            assertThat(pagination.getHasPrev()).isEqualTo(pageIndex != 0);
            assertThat(pagination.getSize()).isEqualTo(BigDecimal.valueOf(pageSize));

            final SortDto sort = getSort(listResponse);
            assertThat(sort).isNotNull();
            assertThat(sort.getDirection()).hasToString(sortDirection);
            assertThat(sort.getField()).isEqualTo(sortField);

            final List<TResponse> results = extractResults(listResponse);
            assertThat(results).isNotNull();
            assertThat(results).hasSize(pageIndex == 3 ? numExpectedDtosLastPage : pageSize);
            foundDtos.addAll(results);
        }

        // then
        final List<TResponse> expectedCombinedResults =
                switch (sortDirection) {
                    case "ASC" -> givenResponses;
                    case "DESC" -> {
                        final List<TResponse> reversed = new ArrayList<>(givenResponses);
                        reversed.sort(getComparator().reversed());
                        yield reversed;
                    }
                    default -> throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
                };
        assertThat(foundDtos)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(getComparisonIgnoreFields())
                .containsExactly(expectedCombinedResults.toArray());
    }

    @Test
    void shouldAllowPageWithoutSort() {
        // given
        final int pageIndex = 0;
        final int pageSize = 5;

        // when
        final ResponseEntity<TListResponse> responsePage = restTemplate.getForEntity(
                endpoint + "?pageIndex={pageIndex}&pageSize={pageSize}", listClass, pageIndex, pageSize);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAllowSortWithoutPage() {
        // given
        final String sortDirection = "DESC";
        final String sortField = getSortField();

        // when
        final ResponseEntity<TListResponse> responsePage = restTemplate.getForEntity(
                endpoint + "?sortDirection={sortDirection}&sortField={sortField}", listClass, sortDirection, sortField);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldShowErrorWhenSortingByUnknownField() {
        // given
        final String unknownField = "unknownField";

        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?" + "pageIndex={pageIndex}&pageSize={pageSize}"
                        + "&sortDirection={sortDirection}&sortField={sortField}",
                ApplicationErrorResponseDto.class,
                0,
                1,
                "ASC",
                unknownField);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final ApplicationErrorResponseDto body = responsePage.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsExactly("sortField");
    }

    @Test
    void shouldShowErrorWhenSortingByUnknownDirection() {
        // given
        final String unknownDirection = "unknownDirection";

        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?" + "pageIndex={pageIndex}&pageSize={pageSize}"
                        + "&sortDirection={sortDirection}&sortField={sortField}",
                ApplicationErrorResponseDto.class,
                0,
                1,
                unknownDirection,
                "name");

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final ApplicationErrorResponseDto body = responsePage.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsExactly("sortDirection");
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, -1", "-1, 1", "-1, -1"})
    void shouldShowErrorWhenInvalidPagination(final int pageIndex, final int pageSize) {
        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?pageIndex={pageIndex}&pageSize={pageSize}",
                ApplicationErrorResponseDto.class,
                pageIndex,
                pageSize);
        final ApplicationErrorResponseDto body = responsePage.getBody();

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsAnyElementsOf(List.of("pageIndex", "pageSize"));
    }

    @Test
    void shouldNotAcceptInvalidUuid() {
        // given
        final String givenInvalidUuid = "invalid uuid";

        // when
        final ResponseEntity<String> serverResponse =
                restTemplate.getForEntity(idEndpoint, String.class, givenInvalidUuid);

        // then
        assertThat(serverResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Generates one test for each search query that subclasses define which assert
     * that the search yields the expected results.
     * Subclasses define in #getSearchparameters() which specific searches need to be
     * tested and what expected results would be.
     */
    @TestFactory
    Stream<DynamicTest> shouldFilterBySearchStringFactory() {
        return getSearchParameters()
                .map(filterTestParameter -> DynamicTest.dynamicTest(
                        filterTestParameter.name, () -> shouldFilterBySearchString(filterTestParameter)));
    }

    /**
     * @see #shouldFilterBySearchStringFactory()
     */
    private void shouldFilterBySearchString(final FilterTestParameter<TResponse> filterTestParameter) {
        // given

        // when
        final String searchTerm = filterTestParameter.searchTerm.apply(filterTestParameter.expectedFound);
        final ResponseEntity<TListResponse> listResponse =
                restTemplate.getForEntity(endpoint + "?filter={searchString}", listClass, searchTerm);

        // then
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TListResponse body = listResponse.getBody();
        assertThat(body).isNotNull();
        final List<TResponse> results = extractResults(body);
        assertThat(results).isNotNull();
        assertThat(results)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(getComparisonIgnoreFields())
                .containsExactly(filterTestParameter.expectedFound);
        assertThat(results).doesNotContain(filterTestParameter.expectedNotFound);
    }

    /**
     * Generate one test for each defined modification that subclasses define which assert
     * that the data changes as expected.
     */
    @TestFactory
    Stream<DynamicTest> shouldUpdateFactory() {
        return getModifications()
                .map(modificationParameter ->
                        DynamicTest.dynamicTest(modificationParameter.name, () -> shouldUpdate(modificationParameter)));
    }

    private void shouldUpdate(final EditTestParameter<TRequest> modification) {
        // given
        final TRequest givenDto = createNew("shouldUpdate", 0);
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);
        final TResponse createResponseBody = checkBody(HttpStatus.CREATED, createResponse, givenDto);
        final String location = Objects.requireNonNull(
                        createResponse.getHeaders().get("Location"))
                .getFirst();

        // when (modify)
        final TRequest modifiedRequest = createRequestFromResponse(createResponseBody);
        modification.modification.accept(modifiedRequest);
        final ResponseEntity<TResponse> modificationResponse =
                restTemplate.exchange(location, HttpMethod.PUT, new HttpEntity<>(modifiedRequest), responseClass);

        // then: modification reflects in the response
        checkBody(HttpStatus.OK, modificationResponse, modifiedRequest);

        // when (read again)
        final ResponseEntity<TResponse> nextReadResponse = restTemplate.getForEntity(location, responseClass);

        // then: modification reflects in next READ
        checkBody(HttpStatus.OK, nextReadResponse, modifiedRequest);
    }

    private TResponse checkBody(
            final HttpStatus expectedStatus,
            final ResponseEntity<TResponse> actualResponse,
            final TRequest givenRequestBody) {
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatus);
        final TResponse actualResponseBody = actualResponse.getBody();
        assertThat(actualResponseBody).isNotNull();
        checkGeneratedIds(actualResponseBody);

        assertThat(actualResponseBody)
                .usingRecursiveComparison()
                .ignoringFields(getComparisonIgnoreFields())
                .isEqualTo(givenRequestBody);
        return actualResponseBody;
    }
}
