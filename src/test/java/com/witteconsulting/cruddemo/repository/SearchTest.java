package com.witteconsulting.cruddemo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class SearchTest {

    @Autowired
    private SearchTestEntityRepository repository;

    @BeforeEach
    void insertTestEntities() {
        repository.deleteAll();
        repository.saveAll(List.of(
                SearchTestEntity.builder().name("Blue Room").capacity(4).build(),
                SearchTestEntity.builder().name("Red Room 7").capacity(12).build(),
                SearchTestEntity.builder().name("Aula").capacity(7).build()));
    }

    @Test
    void shouldMatchAllWhenSearchIsNull() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch(null, "name"));

        assertThat(found).hasSize(3);
    }

    @Test
    void shouldMatchStringFieldCaseInsensitively() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch("BLUE", "name"));

        assertThat(found).extracting(SearchTestEntity::getName).containsExactly("Blue Room");
    }

    @Test
    void shouldMatchStringFieldByFragment() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch("room", "name"));

        assertThat(found).extracting(SearchTestEntity::getName).containsExactlyInAnyOrder("Blue Room", "Red Room 7");
    }

    @Test
    void shouldMatchIntegerFieldByEquality() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch("7", "capacity"));

        assertThat(found).extracting(SearchTestEntity::getName).containsExactly("Aula");
    }

    @Test
    void shouldCombineFieldsWithOr() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch("7", "name", "capacity"));

        assertThat(found).extracting(SearchTestEntity::getName).containsExactlyInAnyOrder("Red Room 7", "Aula");
    }

    @Test
    void shouldMatchNothingWhenNonNumericSearchOnlyHasIntegerFields() {
        final List<SearchTestEntity> found = repository.findAll(Search.matchesSearch("aula", "capacity"));

        assertThat(found).isEmpty();
    }

    @Test
    void shouldMatchNothingWhenNoFieldMatches() {
        final List<SearchTestEntity> found =
                repository.findAll(Search.matchesSearch("does-not-exist", "name", "capacity"));

        assertThat(found).isEmpty();
    }
}
