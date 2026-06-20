package com.cursor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long lowStockProductCount;
    private List<ProductResponse> lowStockProducts;
    private Map<String, Long> ordersByStatus;
    private List<OrderSummaryResponse> recentOrders;
}
