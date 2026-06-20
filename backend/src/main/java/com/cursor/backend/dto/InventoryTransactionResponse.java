package com.cursor.backend.dto;

import com.cursor.backend.entity.InventoryTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionResponse {

    private Long id;
    private Long productId;
    private String productName;
    private InventoryTransactionType type;
    private Integer quantityChange;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;
}
