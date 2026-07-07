package com.witteconsulting.cruddemo;

import static com.witteconsulting.cruddemo.TestUtil.allCombinations;
import static com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto.ErrorTypeEnum.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import com.witteconsulting.cruddemo.model.ApplicationErrorResponseDto;
import com.witteconsulting.cruddemo.model.PaginationDto;
import com.witteconsulting.cruddemo.model.SortDto;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * An abstract base class for general CRUD operations against a REST API.
 * <p>
 * Subclasses need to implement abstract methods to provide all information
 * that is specific to the type of entity.
 *
 * @param <TRequest>      the Dto class
 * @param <TListResponse> the Dto-List-class
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
abstract class AbstractCrudIT<TRequest, TResponse, TListResponse> {
    private final Class<TResponse> responseClass;
    private final Class<TListResponse> listClass;
    private final String endpoint;
    private final String idEndpoint;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * This parameter is used for tests related to search. Both requests are created
     * before searching; the search term derived from {@code requestFound} must match
     * only the entity created from it.
     */
    record FilterTestParameter<TRequest>(
            String name, Function<TRequest, String> searchTerm, TRequest requestFound, TRequest requestNotFound) {}

    /**
     * This parameter defines a modification to existing
     * data. Tests use it to verify that the modification works.
     */
    record EditTestParameter<TRequest>(String name, Consumer<TRequest> modification) {}

    /**
     * This parameter defines a request that must be rejected with a validation error,
     * together with the names of the fields whose values are invalid.
     */
    record InvalidRequestTestParameter<TRequest>(String name, TRequest request, String[] expectedInvalidFieldNames) {}

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
    abstract TRequest templateCreateValidRequest(final String methodName, final int enumerator);

    /**
     * Invalid requests for {@link #shouldRespondBadRequestAndSpecificFieldsForInvalidFactory()}.
     * Each example must trigger a bean validation error.
     */
    abstract Collection<InvalidRequestTestParameter<TRequest>> templateInvalidRequestExamples();

    abstract UUID templateExtractId(TResponse dto);

    abstract String[] templateComparisonIgnoredFields();

    abstract List<TResponse> templateExtractResults(final TListResponse body);

    abstract void templateDeleteAllExisting();

    abstract String templateSortField();

    abstract PaginationDto templateExtractPagination(final TListResponse listResponse);

    abstract SortDto templateExtractSort(final TListResponse listResponse);

    abstract Comparator<TResponse> templateSortComparator();

    /**
     * Search parameters for {@link #shouldFilterBySearchStringFactory()}
     */
    abstract Collection<FilterTestParameter<TRequest>> templateFilterExamples();

    /**
     * Define how to create a new request from a received response in order
     * to do an update (PUT).
     */
    abstract TRequest templateCreateRequestFromResponse(TResponse response);

    /**
     * Modifications to test for {@link #shouldUpdateFactory()}.
     * Implement changes to a DTO here to let the test verify that those
     * are reflected in the response and in the next read.
     */
    Stream<EditTestParameter<TRequest>> templateUpdateModifications() {
        // should override!
        return Stream.of(
                // noop
                new EditTestParameter<>("no change (override method?)", dto -> {}));
    }

    /**
     * Check that an ID has been generated. For check in nested classes, override.
     */
    void templateCheckGeneratedIds(final TResponse responseDto) {
        assertThat(templateExtractId(responseDto)).isNotNull();
    }

    @Test
    void shouldCreateNew() {
        // given
        final TRequest givenDto = templateCreateValidRequest("shouldCreateNew", 1);

        // when
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);

        // then
        final TResponse createBody = createResponse.getBody();
        assertThat(createBody).isNotNull();
        final URI expectedLocation = expectedUriForId(templateExtractId(createBody));
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getHeaders().getLocation()).isEqualTo(expectedLocation);
        assertThat(createBody)
                .usingRecursiveComparison()
                .ignoringFields(templateComparisonIgnoredFields())
                .isEqualTo(givenDto);

        // retrieve newly created object
        final ResponseEntity<TResponse> getResponse = restTemplate.getForEntity(expectedLocation, responseClass);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TResponse findBody = getResponse.getBody();
        assertThat(findBody).isNotNull();
        templateCheckGeneratedIds(findBody);
        assertThat(findBody)
                .usingRecursiveComparison()
                .ignoringFields(templateComparisonIgnoredFields())
                .isEqualTo(givenDto);
    }

    @Test
    void shouldDeleteExisting() {
        // given
        final TRequest givenDto = templateCreateValidRequest("shouldDeleteExisting", 1);
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when
        final URI deleteUri = createResponse.getHeaders().getLocation();
        final ResponseEntity<String> deleteResponse =
                restTemplate.exchange(deleteUri, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        // verify the object is gone
        final ResponseEntity<String> getResponse = restTemplate.getForEntity(deleteUri, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Generates one test per combination of invalid request example and HTTP method
     * (POST and PUT) which asserts that the request is rejected with a validation error
     * naming the invalid fields.
     * Subclasses define the invalid requests in {@link #templateInvalidRequestExamples()}.
     */
    @TestFactory
    Stream<DynamicTest> shouldRespondBadRequestAndSpecificFieldsForInvalidFactory() {
        return allCombinations(
                List.of(HttpMethod.POST, HttpMethod.PUT), templateInvalidRequestExamples(), this::createDynamicTest);
    }

    private @NonNull DynamicTest createDynamicTest(
            final HttpMethod method, final InvalidRequestTestParameter<TRequest> parameter) {
        return DynamicTest.dynamicTest(
                "%s (%s)".formatted(parameter.name, method),
                () -> shouldRespondBadRequestAndSpecificFieldsForInvalid(method, parameter));
    }

    /**
     * @see #shouldRespondBadRequestAndSpecificFieldsForInvalidFactory()
     */
    private void shouldRespondBadRequestAndSpecificFieldsForInvalid(
            final HttpMethod method, final InvalidRequestTestParameter<TRequest> parameter) {
        // given
        // PUT needs an existing entity so the validation error is the only possible failure cause
        final URI requestUri = method.equals(HttpMethod.PUT)
                ? createEntity("shouldRespondBadRequestAndSpecificFieldsForInvalid")
                : URI.create(endpoint);

        // when
        final ResponseEntity<ApplicationErrorResponseDto> response = restTemplate.exchange(
                requestUri, method, new HttpEntity<>(parameter.request), ApplicationErrorResponseDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final ApplicationErrorResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(body.getErrorType()).isEqualTo(VALIDATION_ERROR);
        softly.assertThat(body.getFieldErrors().keySet())
                .containsExactlyInAnyOrder(parameter.expectedInvalidFieldNames);
        softly.assertAll();
    }

    @Test
    void shouldListWithDefaultPagingWhenNoParams() {
        // given
        templateDeleteAllExisting();
        final TRequest given1 = templateCreateValidRequest("shouldListWithDefaultPagingWhenNoParams", 1);
        final TRequest given2 = templateCreateValidRequest("shouldListWithDefaultPagingWhenNoParams", 2);
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
        final List<TResponse> results = templateExtractResults(body);
        assertThat(results)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(templateComparisonIgnoredFields())
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
        templateDeleteAllExisting();
        final List<TResponse> givenResponses = new ArrayList<>();
        final ArrayList<Object> foundDtos = new ArrayList<>();
        final int numGivenDtos = 10;
        final int pageSize = 3;
        final int numExpectedDtosLastPage = 1;
        final int numExpectedPages = 4;
        final String sortField = templateSortField();
        for (int i = 0; i < numGivenDtos; i++) {
            final TRequest given = templateCreateValidRequest("shouldListAllPaginatedAndSorted", i);
            final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, given, responseClass);
            givenResponses.add(Objects.requireNonNull(createResponse.getBody()));
        }

        // when / then
        for (int pageIndex = 0; pageIndex < numExpectedPages; pageIndex++) {
            log.info("Fetching pageIndex {}", pageIndex);
            final ResponseEntity<TListResponse> responsePage = restTemplate.getForEntity(
                    endpoint + "?page={page}&size={size}&sort={sortField},{sortDirection}",
                    listClass,
                    pageIndex,
                    pageSize,
                    sortField,
                    sortDirection);
            assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);

            final TListResponse listResponse = responsePage.getBody();
            assertThat(listResponse).isNotNull();

            final PaginationDto pagination = templateExtractPagination(listResponse);
            assertThat(pagination).isNotNull();
            assertThat(pagination.getTotal()).isEqualTo(BigDecimal.valueOf(numGivenDtos));
            assertThat(pagination.getIndex()).isEqualTo(BigDecimal.valueOf(pageIndex));
            assertThat(pagination.getHasNext()).isEqualTo(pageIndex != 3);
            assertThat(pagination.getHasPrevious()).isEqualTo(pageIndex != 0);
            assertThat(pagination.getSize()).isEqualTo(BigDecimal.valueOf(pageSize));

            final SortDto sort = templateExtractSort(listResponse);
            assertThat(sort).isNotNull();
            assertThat(sort.getDirection()).hasToString(sortDirection);
            assertThat(sort.getField()).isEqualTo(sortField);

            final List<TResponse> results = templateExtractResults(listResponse);
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
                        reversed.sort(templateSortComparator().reversed());
                        yield reversed;
                    }
                    default -> throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
                };
        assertThat(foundDtos)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(templateComparisonIgnoredFields())
                .containsExactly(expectedCombinedResults.toArray());
    }

    @Test
    void shouldAllowPageWithoutSort() {
        // given
        final int pageIndex = 0;
        final int pageSize = 5;

        // when
        final ResponseEntity<TListResponse> responsePage =
                restTemplate.getForEntity(endpoint + "?page={page}&size={size}", listClass, pageIndex, pageSize);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAllowSortWithoutPage() {
        // given
        final String sortDirection = "DESC";
        final String sortField = templateSortField();

        // when
        final ResponseEntity<TListResponse> responsePage = restTemplate.getForEntity(
                endpoint + "?sort={sortField},{sortDirection}", listClass, sortField, sortDirection);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldShowErrorWhenSortingByUnknownField() {
        // given
        final String unknownField = "unknownField";

        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?page={page}&size={size}&sort={sortField},{sortDirection}",
                ApplicationErrorResponseDto.class,
                0,
                1,
                unknownField,
                "ASC");

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final ApplicationErrorResponseDto body = responsePage.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsExactly("sort");
    }

    @Test
    void shouldShowErrorWhenSortingByUnknownDirection() {
        // given
        final String unknownDirection = "unknownDirection";

        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?page={page}&size={size}&sort={sortField},{sortDirection}",
                ApplicationErrorResponseDto.class,
                0,
                1,
                "name",
                unknownDirection);

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final ApplicationErrorResponseDto body = responsePage.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsExactly("sort");
    }

    @Disabled("Spring's Pageable resolver clamps invalid page/size to valid values."
            + " Enable if strict validation is added instead.")
    @ParameterizedTest
    @CsvSource({"0, 0", "0, -1", "-1, 1", "-1, -1"})
    void shouldShowErrorWhenInvalidPagination(final int pageIndex, final int pageSize) {
        // when
        final ResponseEntity<ApplicationErrorResponseDto> responsePage = restTemplate.getForEntity(
                endpoint + "?page={page}&size={size}", ApplicationErrorResponseDto.class, pageIndex, pageSize);
        final ApplicationErrorResponseDto body = responsePage.getBody();

        // then
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull();
        assertThat(body.getErrorType()).isEqualTo(VALIDATION_ERROR);
        assertThat(body.getFieldErrors().keySet()).containsAnyElementsOf(List.of("page", "size"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void shouldReturn404ForNonExistentId(final HttpMethod method) {
        // given
        final UUID nonExistentId = UUID.randomUUID();
        // PUT needs a valid body to reach the id lookup instead of failing validation
        final HttpEntity<?> requestEntity = method.equals(HttpMethod.PUT)
                ? new HttpEntity<>(templateCreateValidRequest("shouldReturn404ForNonExistentId", 1))
                : HttpEntity.EMPTY;

        // when
        final ResponseEntity<String> serverResponse =
                restTemplate.exchange(idEndpoint, method, requestEntity, String.class, nonExistentId);

        // then
        assertThat(serverResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAcceptInvalidUuid() {
        // given
        final String givenInvalidUuid = "invalid uuid";

        // when
        final ResponseEntity<String> serverResponse =
                restTemplate.getForEntity(idEndpoint, String.class, givenInvalidUuid);

        // then
        assertThat(serverResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Generates one test for each search query that subclasses define which assert
     * that the search yields the expected results.
     * Subclasses define in #templateFilterExamples() which specific searches need to be
     * tested and what expected results would be.
     */
    @TestFactory
    Stream<DynamicTest> shouldFilterBySearchStringFactory() {
        return templateFilterExamples().stream()
                .map(filterTestParameter -> DynamicTest.dynamicTest(
                        filterTestParameter.name, () -> shouldFilterBySearchString(filterTestParameter)));
    }

    /**
     * @see #shouldFilterBySearchStringFactory()
     */
    private void shouldFilterBySearchString(final FilterTestParameter<TRequest> filterTestParameter) {
        // given
        templateDeleteAllExisting();
        final ResponseEntity<TResponse> foundCreateResponse =
                restTemplate.postForEntity(endpoint, filterTestParameter.requestFound, responseClass);
        final ResponseEntity<TResponse> notFoundCreateResponse =
                restTemplate.postForEntity(endpoint, filterTestParameter.requestNotFound, responseClass);
        assertThat(foundCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(notFoundCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final TResponse expectedFound = foundCreateResponse.getBody();
        assertThat(expectedFound).isNotNull();

        // when
        final String searchTerm = filterTestParameter.searchTerm.apply(filterTestParameter.requestFound);
        final ResponseEntity<TListResponse> listResponse =
                restTemplate.getForEntity(endpoint + "?filter={searchString}", listClass, searchTerm);

        // then
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final TListResponse body = listResponse.getBody();
        assertThat(body).isNotNull();
        final List<TResponse> results = templateExtractResults(body);
        assertThat(results).isNotNull();
        // containsExactly also asserts that the entity from requestNotFound is absent
        assertThat(results)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(templateComparisonIgnoredFields())
                .containsExactly(expectedFound);
    }

    /**
     * Generate one test for each defined modification that subclasses define which assert
     * that the data changes as expected.
     */
    @TestFactory
    Stream<DynamicTest> shouldUpdateFactory() {
        return templateUpdateModifications()
                .map(modificationParameter ->
                        DynamicTest.dynamicTest(modificationParameter.name, () -> shouldUpdate(modificationParameter)));
    }

    private void shouldUpdate(final EditTestParameter<TRequest> modification) {
        // given
        final TRequest givenDto = templateCreateValidRequest("shouldUpdate", 0);
        final ResponseEntity<TResponse> createResponse = restTemplate.postForEntity(endpoint, givenDto, responseClass);
        final TResponse createResponseBody = checkBody(HttpStatus.CREATED, createResponse, givenDto);
        final URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        // when (modify)
        final TRequest modifiedRequest = templateCreateRequestFromResponse(createResponseBody);
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
        templateCheckGeneratedIds(actualResponseBody);

        assertThat(actualResponseBody)
                .usingRecursiveComparison()
                .ignoringFields(templateComparisonIgnoredFields())
                .isEqualTo(givenRequestBody);
        return actualResponseBody;
    }

    /**
     * Create an entity from a valid request and return its location.
     */
    private URI createEntity(@SuppressWarnings("SameParameterValue") final String methodName) {
        final ResponseEntity<TResponse> createResponse =
                restTemplate.postForEntity(endpoint, templateCreateValidRequest(methodName, 0), responseClass);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return Objects.requireNonNull(createResponse.getHeaders().getLocation());
    }

    private URI expectedUriForId(final UUID uuid) {
        return UriComponentsBuilder.fromPath(endpoint)
                .pathSegment(uuid.toString())
                .build()
                .toUri();
    }
}
