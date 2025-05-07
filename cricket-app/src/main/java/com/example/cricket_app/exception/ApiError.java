package com.example.cricket_app.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApiError {
    private Integer status;
    private String message;
    @Schema(type = "string", format = "date-time", example = "2025-04-30T04:54:29.709Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime time;
    private Map<String, String> errors;

    public ApiError(Integer status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.time = LocalDateTime.now();
        this.errors = errors;
    }
}
