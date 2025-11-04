package com.catchy.service;

import com.catchy.model.*;
import com.catchy.repository.OrderItemRepository;
import com.catchy.repository.OrderRepository;
import com.catchy.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByOrderDateDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderItem> getOrderItems(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    @Transactional
    public Order placeOrder(User user) {
        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Check stock availability
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        }

        // Calculate total
        BigDecimal totalPrice = cartService.getCartTotal(user);

        // Create order
        Order order = new Order(user, totalPrice);
        order = orderRepository.save(order);

        // Create order items and update stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            
            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), itemPrice);
            orderItemRepository.save(orderItem);

            // Update product stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productService.createProduct(product);
        }

        // Clear cart
        cartService.clearCart(user);

        return order;
    }
}

