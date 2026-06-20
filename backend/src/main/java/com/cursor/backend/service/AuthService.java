package com.cursor.backend.service;

import com.cursor.backend.dto.AuthResponse;
import com.cursor.backend.dto.LoginRequest;
import com.cursor.backend.dto.RefreshTokenRequest;
import com.cursor.backend.dto.RegisterRequest;
import com.cursor.backend.entity.Cart;
import com.cursor.backend.entity.RefreshToken;
import com.cursor.backend.entity.Role;
import com.cursor.backend.entity.RoleName;
import com.cursor.backend.entity.User;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.repository.RoleRepository;
import com.cursor.backend.repository.UserRepository;
import com.cursor.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new BadRequestException("Default role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        Cart cart = Cart.builder().user(user).build();
        user.setCart(cart);

        userRepository.save(user);
        log.info("Registered new user: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();
        log.info("Refreshing tokens for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
