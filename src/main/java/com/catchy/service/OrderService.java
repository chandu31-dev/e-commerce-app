package com.catchy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Address;
import com.catchy.model.CartItem;
import com.catchy.model.Order;
import com.catchy.model.OrderItem;
import com.catchy.model.Product;
import com.catchy.model.User;
import com.catchy.repository.AddressRepository;
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

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private com.catchy.service.CouponService couponService;
    @Autowired(required = false)
    private com.catchy.service.MailService mailService;
    @Autowired
    private com.catchy.service.PayoutService payoutService;

    @Autowired
    private com.catchy.repository.VendorProductRepository vendorProductRepository;

    @org.springframework.beans.factory.annotation.Value("${vendor.default.commission-percent:10}")
    private double defaultCommissionPercent;

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
    public Order placeOrder(User user, Long addressId, String couponCode) {
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

        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
        com.catchy.model.Coupon appliedCoupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            com.catchy.model.Coupon coupon = couponService.getCouponByCode(couponCode);
            if (coupon == null) throw new RuntimeException("Invalid coupon code");
            // use CouponService to calculate discount and validate rules
            discountAmount = couponService.calculateDiscount(coupon, user, cartItems, totalPrice);
            appliedCoupon = coupon;
            totalPrice = totalPrice.subtract(discountAmount);
            if (totalPrice.compareTo(java.math.BigDecimal.ZERO) < 0) totalPrice = java.math.BigDecimal.ZERO;
        }

        // Create order
        Order order = new Order(user, totalPrice);
        if (discountAmount != null && discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            order.setDiscountAmount(discountAmount);
            order.setCouponCode(couponCode);
        }
        
        // Set delivery address if provided
        if (addressId != null && addressId > 0) {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            // Verify address belongs to user
            if (!address.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Address does not belong to current user");
            }
            order.setAddress(address);
        }
        
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

            // Create payout record for vendor if product is supplied by a vendor
            try {
                java.util.List<com.catchy.model.VendorProduct> vps = vendorProductRepository.findByProductId(product.getId());
                if (vps != null && !vps.isEmpty()) {
                    // choose the first active vendor product
                    com.catchy.model.VendorProduct chosen = vps.stream().filter(com.catchy.model.VendorProduct::isActive).findFirst().orElse(vps.get(0));
                    com.catchy.model.Vendor vendor = chosen.getVendor();
                    java.math.BigDecimal commission = itemPrice.multiply(java.math.BigDecimal.valueOf(defaultCommissionPercent / 100.0));
                    java.math.BigDecimal vendorAmount = itemPrice.subtract(commission);
                    payoutService.createPayout(vendor, vendorAmount, commission);
                }
            } catch (Exception ex) {
                // don't fail order because payout creation failed; log if necessary
            }
        }

        // Clear cart
        cartService.clearCart(user);

        // Increment coupon usage if applied
        if (appliedCoupon != null) {
            try { couponService.incrementUsage(appliedCoupon); } catch (Exception ex) { /* ignore */ }
        }

        // Send order confirmation email (async inside MailService)
        try {
            if (mailService != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("<p>Hi ").append(user.getName()).append(",</p>");
                sb.append("<p>Thank you for your order #").append(order.getId()).append(".</p>");

                sb.append("<h4>Items:</h4>");
                sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
                sb.append("<tr><th>Product</th><th>Unit Price</th><th>Qty</th><th>Line Total</th></tr>");
                for (OrderItem oi : orderItemRepository.findByOrder(order)) {
                    sb.append("<tr>");
                    sb.append("<td>").append(oi.getProduct().getName()).append("</td>");
                    sb.append("<td>").append(oi.getProduct().getPrice()).append("</td>");
                    sb.append("<td>").append(oi.getQuantity()).append("</td>");
                    sb.append("<td>").append(oi.getPrice()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");

                sb.append("<p><strong>Total: </strong>").append(order.getTotalPrice()).append("</p>");
                if (order.getDiscountAmount() != null) sb.append("<p><strong>Discount: </strong>").append(order.getDiscountAmount()).append("</p>");

                if (order.getAddress() != null) {
                    Address a = order.getAddress();
                    sb.append("<h4>Shipping Address:</h4>");
                    if (a.getLabel() != null && !a.getLabel().isBlank()) sb.append("<p>").append(a.getLabel()).append("</p>");
                    if (a.getAddress() != null && !a.getAddress().isBlank()) sb.append("<p>").append(a.getAddress()).append("</p>");
                    if (a.getPhone() != null && !a.getPhone().isBlank()) sb.append("<p>Phone: ").append(a.getPhone()).append("</p>");
                }

                sb.append("<p>We will notify you when your order is shipped.</p>");
                mailService.sendOrderConfirmationEmail(user.getEmail(), "Order Confirmation - #" + order.getId(), sb.toString());
            }
        } catch (Exception e) {
            // don't fail order because of email issues
        }

        return order;
    }
}

