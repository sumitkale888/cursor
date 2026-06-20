package com.cursor.backend.controller;

import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.CartItemRequest;
import com.cursor.backend.dto.CartResponse;
import com.cursor.backend.dto.UpdateCartItemRequest;
import com.cursor.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.ok(cartService.addItem(userDetails.getUsername(), request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ApiResponse.ok(cartService.removeItem(userDetails.getUsername(), productId));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.ok(cartService.updateQuantity(userDetails.getUsername(), productId, request));
    }
}
