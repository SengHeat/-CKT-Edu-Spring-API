package com.ckt.api.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiError {

    // getters and setters
    private final int status;
    private final String title;
    private final String detail;
    private final Instant timestamp;
    private final String path;
    private final String errorCode;

    public ApiError(int status, String title, String detail, String path, String errorCode) {
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.timestamp = Instant.now();
        this.path = path;
        this.errorCode = errorCode;
    }
}
