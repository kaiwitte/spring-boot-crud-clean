package com.witteconsulting.cruddemo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationException extends RuntimeException {
    private final String argument;
    private final String message;
}
