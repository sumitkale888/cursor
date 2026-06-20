package com.cursor.backend.repository;

import com.cursor.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Optional<Review> findByIdAndUserId(Long id, Long userId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRating(Long productId);

    long countByProductId(Long productId);
}
