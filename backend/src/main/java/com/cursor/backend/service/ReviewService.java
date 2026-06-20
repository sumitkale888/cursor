package com.cursor.backend.service;

import com.cursor.backend.dto.ReviewRequest;
import com.cursor.backend.dto.ReviewResponse;
import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.entity.Product;
import com.cursor.backend.entity.Review;
import com.cursor.backend.entity.User;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.ProductRepository;
import com.cursor.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);
        return PagedResponse.from(page.map(entityMapper::toReviewResponse));
    }

    @Transactional
    public ReviewResponse addReview(String email, Long productId, ReviewRequest request) {
        User user = userService.getUserByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        updateProductRating(product);
        log.info("Added review id={} for product id={}", saved.getId(), productId);
        return entityMapper.toReviewResponse(saved);
    }

    @Transactional
    public ReviewResponse updateReview(String email, Long reviewId, ReviewRequest request) {
        User user = userService.getUserByEmail(email);
        Review review = reviewRepository.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct());
        log.info("Updated review id={}", reviewId);
        return entityMapper.toReviewResponse(saved);
    }

    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = userService.getUserByEmail(email);
        Review review = reviewRepository.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
        log.info("Deleted review id={}", reviewId);
    }

    private void updateProductRating(Product product) {
        Double average = reviewRepository.calculateAverageRating(product.getId());
        long count = reviewRepository.countByProductId(product.getId());
        product.setAverageRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        product.setReviewCount((int) count);
        productRepository.save(product);
    }
}
