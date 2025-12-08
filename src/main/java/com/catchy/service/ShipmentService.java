package com.catchy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catchy.model.Order;
import com.catchy.model.Shipment;
import com.catchy.repository.ShipmentRepository;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;
    private final com.catchy.service.MailService mailService;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderService orderService, com.catchy.service.MailService mailService) {
        this.shipmentRepository = shipmentRepository;
        this.orderService = orderService;
        this.mailService = mailService;
    }

    @Transactional
    public Shipment createShipment(Long orderId, String carrier, String trackingNumber, LocalDateTime estimatedDelivery) {
        Order order = orderService.getOrderById(orderId);
        Shipment s = new Shipment(order, carrier, trackingNumber);
        s.setEstimatedDelivery(estimatedDelivery);
        s.setStatus(Shipment.Status.SHIPPED);
        Shipment saved = shipmentRepository.save(s);
        // notify customer if mail service available
        try {
            if (mailService != null) {
                String body = String.format("Your order #%d has been shipped via %s. Tracking: %s", order.getId(), carrier, trackingNumber);
                mailService.sendOrderConfirmationEmail(order.getUser().getEmail(), "Your order has shipped", body);
            }
        } catch (Exception ex) {
            // ignore email errors
        }
        return saved;
    }

    public Optional<Shipment> findByTracking(String tracking) {
        return shipmentRepository.findByTrackingNumber(tracking);
    }

    public List<Shipment> getShipmentsForOrder(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Shipment updateShipmentStatus(Long shipmentId, Shipment.Status status, String eventNote) {
        Shipment s = shipmentRepository.findById(shipmentId).orElseThrow(() -> new RuntimeException("Shipment not found"));
        s.setStatus(status);
        s.setUpdatedAt(LocalDateTime.now());
        if (eventNote != null && !eventNote.isBlank()) {
            String prev = s.getEvents() == null ? "" : s.getEvents() + "\n";
            s.setEvents(prev + LocalDateTime.now() + ": " + eventNote);
        }
        Shipment saved = shipmentRepository.save(s);
        // notify customer
        try {
            if (mailService != null) {
                String body = String.format("Shipment update for order #%d: %s (tracking: %s)", s.getOrder().getId(), status, s.getTrackingNumber());
                mailService.sendOrderConfirmationEmail(s.getOrder().getUser().getEmail(), "Shipment update", body);
            }
        } catch (Exception ex) {
            // ignore
        }
        return saved;
    }
}
