package com.cursor.backend.service;

import com.cursor.backend.dto.AdminDashboardResponse;
import com.cursor.backend.dto.OrderSummaryResponse;
import com.cursor.backend.entity.OrderStatus;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.OrderRepository;
import com.cursor.backend.repository.ProductRepository;
import com.cursor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final EntityMapper entityMapper;

    @Value("${app.inventory.low-stock-threshold}")
    private int lowStockThreshold;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Map<String, Long> ordersByStatus = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        orderRepository::countByStatus,
                        (a, b) -> a,
                        LinkedHashMap::new));

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalOrders(orderRepository.count())
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .lowStockProductCount(productRepository.countByStockLessThanEqual(lowStockThreshold))
                .lowStockProducts(productService.getLowStockProducts(lowStockThreshold))
                .ordersByStatus(ordersByStatus)
                .recentOrders(orderRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(entityMapper::toOrderSummaryResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderSummaryResponse> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).stream()
                .map(entityMapper::toOrderSummaryResponse)
                .collect(Collectors.toList());
    }
}
