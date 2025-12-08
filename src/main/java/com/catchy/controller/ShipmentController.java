package com.catchy.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.model.Shipment;
import com.catchy.service.ShipmentService;

@RestController
@RequestMapping("/api")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // Admin: create shipment for order
    @PostMapping("/admin/orders/{orderId}/ship")
    public ResponseEntity<?> createShipment(@PathVariable Long orderId,
                                            @RequestParam String carrier,
                                            @RequestParam String trackingNumber,
                                            @RequestParam(required = false) String estimatedDeliveryIso) {
        LocalDateTime est = null;
        if (estimatedDeliveryIso != null && !estimatedDeliveryIso.isBlank()) {
            est = LocalDateTime.parse(estimatedDeliveryIso);
        }
        Shipment s = shipmentService.createShipment(orderId, carrier, trackingNumber, est);
        return ResponseEntity.ok(s);
    }

    // Public: get shipment by tracking
    @GetMapping("/shipments/{tracking}")
    public ResponseEntity<?> track(@PathVariable String tracking) {
        return shipmentService.findByTracking(tracking)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin: list shipments for order
    @GetMapping("/admin/orders/{orderId}/shipments")
    public ResponseEntity<List<Shipment>> listForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentsForOrder(orderId));
    }
}
