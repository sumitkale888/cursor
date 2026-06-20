package com.cursor.backend.mapper;

import com.cursor.backend.dto.*;
import com.cursor.backend.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toProductResponseList(List<Product> products) {
        return products.stream().map(this::toProductResponse).collect(Collectors.toList());
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public WishlistItemResponse toWishlistItemResponse(WishlistItem item) {
        Product product = item.getProduct();
        return WishlistItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .price(product.getPrice())
                .averageRating(product.getAverageRating())
                .addedAt(item.getCreatedAt())
                .build();
    }

    public WishlistResponse toWishlistResponse(List<WishlistItem> items) {
        List<WishlistItemResponse> itemResponses = items.stream()
                .map(this::toWishlistItemResponse)
                .collect(Collectors.toList());
        return WishlistResponse.builder()
                .id(items.isEmpty() ? null : items.getFirst().getUser().getId())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .build();
    }

    public OrderStatusEventResponse toOrderStatusEventResponse(OrderStatusHistory history) {
        return OrderStatusEventResponse.builder()
                .status(history.getStatus())
                .note(history.getNote())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public InventoryTransactionResponse toInventoryTransactionResponse(InventoryTransaction transaction) {
        return InventoryTransactionResponse.builder()
                .id(transaction.getId())
                .productId(transaction.getProduct().getId())
                .productName(transaction.getProduct().getName())
                .type(transaction.getType())
                .quantityChange(transaction.getQuantityChange())
                .previousStock(transaction.getPreviousStock())
                .newStock(transaction.getNewStock())
                .reason(transaction.getReason())
                .performedBy(transaction.getPerformedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        List<OrderStatusEventResponse> statusHistory = order.getStatusHistory().stream()
                .map(this::toOrderStatusEventResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress() != null
                        ? toAddressResponse(order.getDeliveryAddress()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .statusHistory(statusHistory)
                .build();
    }

    public OrderSummaryResponse toOrderSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .itemCount(order.getItems().size())
                .build();
    }
}
