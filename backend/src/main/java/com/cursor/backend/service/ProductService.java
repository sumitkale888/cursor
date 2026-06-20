package com.cursor.backend.service;

import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.dto.ProductRequest;
import com.cursor.backend.dto.ProductResponse;
import com.cursor.backend.entity.Category;
import com.cursor.backend.entity.Product;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.CategoryRepository;
import com.cursor.backend.repository.ProductRepository;
import com.cursor.backend.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(
            String query,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating,
            Pageable pageable) {
        Page<Product> page = productRepository.findAll(
                ProductSpecification.withFilters(query, categoryId, minPrice, maxPrice, minRating),
                pageable);
        return PagedResponse.from(page.map(entityMapper::toProductResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return entityMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(String query, Pageable pageable) {
        return getProducts(query, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return getProducts(null, categoryId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return entityMapper.toProductResponseList(productRepository.findByStockLessThanEqual(threshold));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={} name={}", saved.getId(), saved.getName());
        return entityMapper.toProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        log.info("Updated product id={}", saved.getId());
        return entityMapper.toProductResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }
}
