package com.witteconsulting.cruddemo.controller;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only endpoints that trigger specific exceptions so integration tests
 * can assert the wire-level error contract.
 */
@RestController
class ErrorHandlingTestController {

    public static final String DO_NOT_LEAK_SERVER_ERROR = "badPropertyPath";

    /** Throws a PropertyReferenceException that does not originate from a sort parameter. */
    @GetMapping("/test-errors/property-reference")
    String propertyReference() {
        PropertyPath.from(DO_NOT_LEAK_SERVER_ERROR, RoomEntity.class); // will throw PropertyReferenceException
        throw new AssertionError("unreachable code; should throw exception above");
    }

    @GetMapping("/test-errors/runtime-exception")
    String runtimeException() {
        throw new IllegalStateException("testdetail");
    }
}
