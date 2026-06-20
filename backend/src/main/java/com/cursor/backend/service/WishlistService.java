package com.cursor.backend.service;

import com.cursor.backend.dto.WishlistResponse;
import com.cursor.backend.entity.Product;
import com.cursor.backend.entity.User;
import com.cursor.backend.entity.WishlistItem;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.ProductRepository;
import com.cursor.backend.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String email) {
        User user = userService.getUserByEmail(email);
        List<WishlistItem> items = wishlistItemRepository.findByUserIdWithProduct(user.getId());
        WishlistResponse response = entityMapper.toWishlistResponse(items);
        response.setId(user.getId());
        return response;
    }

    @Transactional
    public WishlistResponse addToWishlist(String email, Long productId) {
        User user = userService.getUserByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("Product is already in wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();
        wishlistItemRepository.save(item);
        log.info("Added product id={} to wishlist for user {}", productId, email);

        return getWishlist(email);
    }

    @Transactional
    public WishlistResponse removeFromWishlist(String email, Long productId) {
        User user = userService.getUserByEmail(email);
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));

        wishlistItemRepository.delete(item);
        log.info("Removed product id={} from wishlist for user {}", productId, email);
        return getWishlist(email);
    }
}
