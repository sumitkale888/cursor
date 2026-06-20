package com.cursor.backend.controller;

import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.OrderResponse;
import com.cursor.backend.dto.OrderStatusEventResponse;
import com.cursor.backend.dto.OrderSummaryResponse;
import com.cursor.backend.dto.PlaceOrderRequest;
import com.cursor.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place order from cart")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.created(orderService.placeOrder(userDetails.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "Get order history")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(orderService.getOrderHistory(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrderDetails(userDetails.getUsername(), id));
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Track order status timeline")
    public ResponseEntity<ApiResponse<List<OrderStatusEventResponse>>> getOrderTracking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrderTracking(userDetails.getUsername(), id));
    }
}
\\h