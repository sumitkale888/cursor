package com.cursor.backend.controller;

import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.dto.ProductRequest;
import com.cursor.backend.dto.ProductResponse;
import com.cursor.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products with pagination, sorting and filtering")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(productService.getProducts(q, categoryId, minPrice, maxPrice, minRating, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create a product (admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.created(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product (admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.ok(null);
    }
}
