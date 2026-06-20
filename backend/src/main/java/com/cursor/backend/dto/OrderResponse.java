package com.cursor.backend.dto;

import com.cursor.backend.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private AddressResponse deliveryAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusEventResponse> statusHistory;
}
