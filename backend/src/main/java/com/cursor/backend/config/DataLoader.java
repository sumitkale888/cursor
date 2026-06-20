package com.cursor.backend.config;

import com.cursor.backend.entity.*;
import com.cursor.backend.repository.CategoryRepository;
import com.cursor.backend.repository.ProductRepository;
import com.cursor.backend.repository.RoleRepository;
import com.cursor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedCategoriesAndProducts();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name(RoleName.USER).build());
            roleRepository.save(Role.builder().name(RoleName.ADMIN).build());
            log.info("Seeded roles: USER, ADMIN");
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@blinkit.com")) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow();
            Role userRole = roleRepository.findByName(RoleName.USER)
                    .orElseThrow();

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@blinkit.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole, userRole))
                    .build();

            Cart cart = Cart.builder().user(admin).build();
            admin.setCart(cart);

            userRepository.save(admin);
            log.info("Seeded admin user: admin@blinkit.com / admin123");
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category fruits = categoryRepository.save(Category.builder()
                .name("Fruits & Vegetables")
                .description("Fresh fruits and vegetables")
                .imageUrl("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=360/app/images/category/cms_images/default/fnv.png")
                .build());

        Category dairy = categoryRepository.save(Category.builder()
                .name("Dairy & Breakfast")
                .description("Milk, bread, eggs and more")
                .imageUrl("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=360/app/images/category/cms_images/default/dairy.png")
                .build());

        Category snacks = categoryRepository.save(Category.builder()
                .name("Snacks & Munchies")
                .description("Chips, biscuits and namkeen")
                .imageUrl("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=360/app/images/category/cms_images/default/snacks.png")
                .build());

        Category beverages = categoryRepository.save(Category.builder()
                .name("Cold Drinks & Juices")
                .description("Soft drinks, juices and energy drinks")
                .imageUrl("https://cdn.grofers.com/cdn-cgi/image/f=auto,fit=scale-down,q=70,metadata=none,w=360/app/images/category/cms_images/default/beverages.png")
                .build());

        productRepository.saveAll(java.util.List.of(
                Product.builder().name("Banana (Robusta)").description("Fresh ripe bananas, 6 pcs").price(new BigDecimal("35.00")).stock(100).category(fruits).build(),
                Product.builder().name("Tomato Hybrid").description("Fresh red tomatoes, 500g").price(new BigDecimal("28.00")).stock(80).category(fruits).build(),
                Product.builder().name("Onion").description("Fresh onions, 1kg").price(new BigDecimal("32.00")).stock(120).category(fruits).build(),
                Product.builder().name("Potato").description("Fresh potatoes, 1kg").price(new BigDecimal("30.00")).stock(150).category(fruits).build(),
                Product.builder().name("Amul Taaza Toned Milk").description("500ml pack").price(new BigDecimal("28.00")).stock(200).category(dairy).build(),
                Product.builder().name("Britannia Brown Bread").description("400g loaf").price(new BigDecimal("45.00")).stock(60).category(dairy).build(),
                Product.builder().name("Farm Fresh Eggs").description("6 pcs tray").price(new BigDecimal("55.00")).stock(90).category(dairy).build(),
                Product.builder().name("Lay's Classic Salted").description("52g pack").price(new BigDecimal("20.00")).stock(300).category(snacks).build(),
                Product.builder().name("Parle-G Gold Biscuits").description("1kg pack").price(new BigDecimal("75.00")).stock(150).category(snacks).build(),
                Product.builder().name("Haldiram's Aloo Bhujia").description("400g pack").price(new BigDecimal("85.00")).stock(70).category(snacks).build(),
                Product.builder().name("Coca-Cola").description("750ml bottle").price(new BigDecimal("40.00")).stock(180).category(beverages).build(),
                Product.builder().name("Real Fruit Power Orange").description("1L tetra pack").price(new BigDecimal("110.00")).stock(50).category(beverages).build(),
                Product.builder().name("Red Bull Energy Drink").description("250ml can").price(new BigDecimal("125.00")).stock(40).category(beverages).build()
        ));

        log.info("Seeded categories and products");
    }
}
