package com.ckt.api.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ApiResponse<T>(
        Boolean status,
        Integer code,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timeStamp,
        @JsonProperty("data") T data) {

    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(true)
                .code(HttpStatus.OK.value())
                .message("Success")
                .timeStamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(true)
                .code(HttpStatus.OK.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status(true)
                .code(HttpStatus.CREATED.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    // Error responses
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(status.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(code)
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.NOT_FOUND.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.FORBIDDEN.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return ApiResponse.<T>builder()
                .status(false)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .data(null)
                .build();
    }
}