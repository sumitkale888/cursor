package com.cursor.backend.controller;

import com.cursor.backend.dto.AdminDashboardResponse;
import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.InventoryTransactionResponse;
import com.cursor.backend.dto.InventoryUpdateRequest;
import com.cursor.backend.dto.OrderResponse;
import com.cursor.backend.dto.OrderSummaryResponse;
import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.dto.UpdateOrderStatusRequest;
import com.cursor.backend.service.AdminService;
import com.cursor.backend.service.InventoryService;
import com.cursor.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard metrics")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ApiResponse.ok(adminService.getDashboard());
    }

    @GetMapping("/orders")
    @Operation(summary = "List all orders")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminService.getAllOrders(page, size));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.ok(orderService.updateOrderStatus(id, request));
    }

    @PutMapping("/inventory/products/{productId}")
    @Operation(summary = "Update product inventory")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> updateInventory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        return ApiResponse.ok(inventoryService.updateStock(productId, request, userDetails.getUsername()));
    }

    @GetMapping("/inventory/products/{productId}/transactions")
    @Operation(summary = "Get inventory transactions for a product")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryTransactionResponse>>> getProductInventoryTransactions(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(inventoryService.getProductTransactions(productId, pageable));
    }

    @GetMapping("/inventory/transactions")
    @Operation(summary = "Get all inventory transactions")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryTransactionResponse>>> getAllInventoryTransactions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(inventoryService.getAllTransactions(pageable));
    }
}
