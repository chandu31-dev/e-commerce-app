package com.catchy.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.ProductRepository;
import com.catchy.repository.UserRepository;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (userRepository.findByEmail("admin@catchy.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@catchy.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: admin@catchy.com / admin123");
        }

        // Create test user if not exists
        if (userRepository.findByEmail("user@catchy.com").isEmpty()) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("user@catchy.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("Test user created: user@catchy.com / user123");
        }

        // Create sample products if database is empty
        if (productRepository.count() == 0) {
            // Electronics
            productRepository.save(new Product(
                "iPhone 15 Pro",
                "Latest iPhone with A17 Pro chip, 48MP camera, and titanium design",
                "Electronics",
                new BigDecimal("999.99"),
                "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=500",
                50
            ));

            productRepository.save(new Product(
                "Samsung Galaxy S24",
                "Premium Android smartphone with AI features and advanced camera system",
                "Electronics",
                new BigDecimal("899.99"),
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500",
                40
            ));

            productRepository.save(new Product(
                "MacBook Pro 16\"",
                "Powerful laptop with M3 chip, perfect for professionals and creatives",
                "Electronics",
                new BigDecimal("2499.99"),
                "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=500",
                25
            ));

            productRepository.save(new Product(
                "Sony WH-1000XM5 Headphones",
                "Premium noise-cancelling wireless headphones with exceptional sound quality",
                "Electronics",
                new BigDecimal("399.99"),
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                60
            ));

            // Fashion
            productRepository.save(new Product(
                "Classic Leather Jacket",
                "Premium genuine leather jacket, timeless design, perfect fit",
                "Fashion",
                new BigDecimal("299.99"),
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500",
                30
            ));

            productRepository.save(new Product(
                "Designer Sunglasses",
                "Stylish UV protection sunglasses with polarized lenses",
                "Fashion",
                new BigDecimal("149.99"),
                "https://images.unsplash.com/photo-1572635196237-14b3f281fbcf?w=500",
                75
            ));

            productRepository.save(new Product(
                "Running Shoes",
                "Comfortable athletic shoes with advanced cushioning technology",
                "Fashion",
                new BigDecimal("129.99"),
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500",
                100
            ));

            // Books
            productRepository.save(new Product(
                "The Great Gatsby",
                "Classic American novel by F. Scott Fitzgerald",
                "Books",
                new BigDecimal("12.99"),
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500",
                200
            ));

            productRepository.save(new Product(
                "Clean Code",
                "A Handbook of Agile Software Craftsmanship by Robert C. Martin",
                "Books",
                new BigDecimal("49.99"),
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500",
                150
            ));

            productRepository.save(new Product(
                "The Pragmatic Programmer",
                "Your Journey to Mastery by Andrew Hunt and David Thomas",
                "Books",
                new BigDecimal("44.99"),
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500",
                120
            ));

            // Home & Garden
            productRepository.save(new Product(
                "Smart LED Light Bulbs",
                "WiFi enabled LED bulbs with color changing capabilities",
                "Home & Garden",
                new BigDecimal("29.99"),
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=500",
                80
            ));

            productRepository.save(new Product(
                "Indoor Plant Set",
                "Beautiful collection of 5 low-maintenance indoor plants",
                "Home & Garden",
                new BigDecimal("79.99"),
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=500",
                45
            ));

            // Sports
            productRepository.save(new Product(
                "Yoga Mat Premium",
                "Eco-friendly, non-slip yoga mat with carrying strap",
                "Sports",
                new BigDecimal("39.99"),
                "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=500",
                90
            ));

            productRepository.save(new Product(
                "Dumbbell Set",
                "Adjustable dumbbell set, perfect for home workouts",
                "Sports",
                new BigDecimal("199.99"),
                "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=500",
                35
            ));

            System.out.println("Sample products created!");
        }
    }
}

