package com.catchy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.model.Shipment;
import com.catchy.service.ShipmentService;

@RestController
@RequestMapping("/api/webhooks/courier")
public class CourierWebhookController {

    private final ShipmentService shipmentService;

    public static class CourierEvent {
        public String trackingNumber;
        public String status; // e.g., IN_TRANSIT, DELIVERED
        public String note;
    }

    public CourierWebhookController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody CourierEvent event) {
        if (event == null || event.trackingNumber == null) return ResponseEntity.badRequest().build();
        return shipmentService.findByTracking(event.trackingNumber).map(s -> {
            try {
                Shipment.Status st = Shipment.Status.valueOf(event.status);
                shipmentService.updateShipmentStatus(s.getId(), st, event.note);
                return ResponseEntity.ok().build();
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body("Invalid status");
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
