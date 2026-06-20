package com.cursor.backend.dto;

import com.cursor.backend.entity.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    @NotNull(message = "Transaction type is required")
    private InventoryTransactionType type;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
