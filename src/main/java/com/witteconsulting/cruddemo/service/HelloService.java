package com.witteconsulting.cruddemo.service;

import com.witteconsulting.cruddemo.api.HelloApiDelegate;
import com.witteconsulting.cruddemo.model.Hello200ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class HelloService implements HelloApiDelegate {

    @Override
    public ResponseEntity<Hello200ResponseDto> hello() {
        final Hello200ResponseDto response = new Hello200ResponseDto()
                .message("Hello, World!");

        return ResponseEntity.ok(response);
    }
}