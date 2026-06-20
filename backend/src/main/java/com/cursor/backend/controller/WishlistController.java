package com.cursor.backend.controller;

import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.WishlistResponse;
import com.cursor.backend.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get user wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(wishlistService.getWishlist(userDetails.getUsername()));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ApiResponse.ok(wishlistService.addToWishlist(userDetails.getUsername(), productId));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ApiResponse.ok(wishlistService.removeFromWishlist(userDetails.getUsername(), productId));
    }
}
