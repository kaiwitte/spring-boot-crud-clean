package com.witteconsulting.cruddemo.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;

/**
 * Test-only repository executing {@link Search} specifications against {@link SearchTestEntity}.
 */
public interface SearchTestEntityRepository
        extends ListCrudRepository<SearchTestEntity, Long>, JpaSpecificationExecutor<SearchTestEntity> {}
