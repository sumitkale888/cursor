package com.cursor.backend.dto;

import com.cursor.backend.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusEventResponse {

    private OrderStatus status;
    private String note;
    private LocalDateTime createdAt;
}
