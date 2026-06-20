package com.cursor.backend.service;

import com.cursor.backend.dto.OrderResponse;
import com.cursor.backend.dto.OrderSummaryResponse;
import com.cursor.backend.dto.PlaceOrderRequest;
import com.cursor.backend.dto.OrderStatusEventResponse;
import com.cursor.backend.dto.UpdateOrderStatusRequest;
import com.cursor.backend.entity.*;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.CartRepository;
import com.cursor.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final InventoryService inventoryService;
    private final EntityMapper entityMapper;

    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User user = userService.getUserByEmail(email);
        Address address = addressService.getUserAddress(email, request.getAddressId());

        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int previousStock = product.getStock();
            product.setStock(previousStock - cartItem.getQuantity());
            inventoryService.recordSale(product, cartItem.getQuantity(), previousStock, email);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        OrderStatusHistory initialStatus = OrderStatusHistory.builder()
                .status(OrderStatus.PENDING)
                .note("Order placed")
                .build();

        Order order = Order.builder()
                .user(user)
                .deliveryAddress(address)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .items(orderItems)
                .statusHistory(new ArrayList<>(List.of(initialStatus)))
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        initialStatus.setOrder(order);

        cart.getItems().clear();

        Order savedOrder = orderRepository.save(order);
        log.info("Placed order id={} for user {}", savedOrder.getId(), email);

        return entityMapper.toOrderResponse(
                orderRepository.findByIdAndUserIdWithDetails(savedOrder.getId(), user.getId())
                        .orElse(savedOrder));
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrderHistory(String email) {
        User user = userService.getUserByEmail(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(entityMapper::toOrderSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(String email, Long orderId) {
        User user = userService.getUserByEmail(email);
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return entityMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusEventResponse> getOrderTracking(String email, Long orderId) {
        OrderResponse order = getOrderDetails(email, orderId);
        return order.getStatusHistory();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot update a delivered order");
        }

        if (request.getStatus() == OrderStatus.CANCELLED) {
            restoreStockOnCancellation(order);
        }

        order.setStatus(request.getStatus());

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(request.getStatus())
                .note(request.getNote())
                .build();
        order.getStatusHistory().add(history);

        Order saved = orderRepository.save(order);
        log.info("Updated order id={} status to {}", orderId, request.getStatus());
        return entityMapper.toOrderResponse(
                orderRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    private void restoreStockOnCancellation(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }
    }
}
