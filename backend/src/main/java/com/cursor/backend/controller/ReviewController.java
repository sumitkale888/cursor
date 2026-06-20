package com.cursor.backend.controller;

import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.dto.ReviewRequest;
import com.cursor.backend.dto.ReviewResponse;
import com.cursor.backend.service.ReviewService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    @Operation(summary = "List product reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(reviewService.getProductReviews(productId, pageable));
    }

    @PostMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Add a product review")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.created(reviewService.addReview(userDetails.getUsername(), productId, request));
    }

    @PutMapping("/api/reviews/{reviewId}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(reviewService.updateReview(userDetails.getUsername(), reviewId, request));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ApiResponse.ok(null);
    }
}
