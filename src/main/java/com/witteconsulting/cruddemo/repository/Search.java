package com.witteconsulting.cruddemo.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class Search {
    public static <T> Specification<T> matchesSearch(final String search, final String... searchFields) {
        if (search == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> Arrays.stream(searchFields)
                .map(field -> getPredicate(search, field, root, cb))
                .flatMap(Optional::stream)
                .reduce(cb::or)
                .orElse(cb.disjunction());
    }

    private static <T> Optional<Predicate> getPredicate(
            final String search, final String searchField, final Root<T> root, final CriteriaBuilder cb) {
        final var fieldType = root.get(searchField).getModel().getBindableJavaType();
        // add other types as needed, or the else branch will fail. add to SearchTest as needed.
        if (fieldType.equals(Integer.class)) {
            return parseOptionalInteger(search).map(intSearch -> cb.equal(root.get(searchField), intSearch));
        } else {
            final var likeString = "%%%s%%".formatted(search.toLowerCase());
            return Optional.of(cb.like(cb.lower(root.get(searchField)), likeString));
        }
    }

    private static Optional<Integer> parseOptionalInteger(final String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }
}
