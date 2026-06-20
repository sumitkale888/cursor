package com.cursor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {

    private Long id;
    private String status;
    private java.math.BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private Integer itemCount;
}
