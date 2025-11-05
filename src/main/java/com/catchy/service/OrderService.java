package com.catchy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.CartItem;
import com.catchy.model.Order;
import com.catchy.model.OrderItem;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.OrderItemRepository;
import com.catchy.repository.OrderRepository;

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

    @Transactional
    public Order updateOrderStatus(Long orderId, com.catchy.model.Order.Status status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}

