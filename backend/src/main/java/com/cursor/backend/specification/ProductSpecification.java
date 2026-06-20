package com.cursor.backend.specification;

import com.cursor.backend.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withFilters(
            String query,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating) {
        return Specification.where(hasSearchQuery(query))
                .and(hasCategoryId(categoryId))
                .and(hasMinPrice(minPrice))
                .and(hasMaxPrice(maxPrice))
                .and(hasMinRating(minRating));
    }

    private static Specification<Product> hasSearchQuery(String query) {
        return (root, criteriaQuery, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, criteriaQuery, cb) -> categoryId == null
                ? cb.conjunction()
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, criteriaQuery, cb) -> minPrice == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, criteriaQuery, cb) -> maxPrice == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static Specification<Product> hasMinRating(BigDecimal minRating) {
        return (root, criteriaQuery, cb) -> minRating == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }
}
