package com.cursor.backend.repository;

import com.cursor.backend.entity.Order;
import com.cursor.backend.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> com.cursor.backend.entity.OrderStatus.CANCELLED")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.deliveryAddress " +
            "LEFT JOIN FETCH o.statusHistory " +
            "WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.deliveryAddress " +
            "LEFT JOIN FETCH o.statusHistory " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
