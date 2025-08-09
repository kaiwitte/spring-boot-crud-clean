package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.HelloApiDelegate;
import com.witteconsulting.cruddemo.model.HelloResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class HelloService implements HelloApiDelegate {

    @Override
    public ResponseEntity<HelloResponseDto> hello() {
        final HelloResponseDto response = new HelloResponseDto()
                .message("Hello, World!");

        return ResponseEntity.ok(response);
    }
}