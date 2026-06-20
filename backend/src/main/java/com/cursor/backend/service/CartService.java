package com.cursor.backend.service;

import com.cursor.backend.dto.CartItemRequest;
import com.cursor.backend.dto.CartResponse;
import com.cursor.backend.dto.UpdateCartItemRequest;
import com.cursor.backend.entity.Cart;
import com.cursor.backend.entity.CartItem;
import com.cursor.backend.entity.Product;
import com.cursor.backend.entity.User;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.CartItemRepository;
import com.cursor.backend.repository.CartRepository;
import com.cursor.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return entityMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
        log.info("Added product id={} to cart for user {}", request.getProductId(), email);
        return entityMapper.toCartResponse(getOrCreateCart(email));
    }

    @Transactional
    public CartResponse removeItem(String email, Long productId) {
        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart with id: " + productId));

        cart.getItems().remove(cartItem);
        cartRepository.save(cart);
        log.info("Removed product id={} from cart for user {}", productId, email);
        return entityMapper.toCartResponse(getOrCreateCart(email));
    }

    @Transactional
    public CartResponse updateQuantity(String email, Long productId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart with id: " + productId));

        Product product = cartItem.getProduct();
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        return entityMapper.toCartResponse(getOrCreateCart(email));
    }

    private Cart getOrCreateCart(String email) {
        User user = userService.getUserByEmail(email);

        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }
}
