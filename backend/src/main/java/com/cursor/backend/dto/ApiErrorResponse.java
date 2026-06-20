package com.cursor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    @Builder.Default
    private boolean success = false;
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;
}
