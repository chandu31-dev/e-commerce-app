package com.catchy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catchy.model.Address;
import com.catchy.model.User;
import com.catchy.repository.AddressRepository;
import com.catchy.service.AuthService;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<List<Address>> list() {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(addressRepository.findByUserId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Address> add(@RequestBody Address address) {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        address.setUser(user);
        // If this address is marked default, unset other defaults for this user
        if (address.isDefault()) {
            List<Address> existing = addressRepository.findByUserId(user.getId());
            for (Address a : existing) {
                if (a.isDefault()) {
                    a.setDefault(false);
                }
            }
            addressRepository.saveAll(existing);
        }
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getById(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        Address addr = addressRepository.findById(id).orElse(null);
        if (addr == null || !addr.getUser().getId().equals(user.getId())) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(addr);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> update(@PathVariable Long id, @RequestBody Address address) {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        Address existing = addressRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) return ResponseEntity.status(404).build();
        existing.setLabel(address.getLabel());
        existing.setAddress(address.getAddress());
        existing.setLatitude(address.getLatitude());
        existing.setLongitude(address.getLongitude());
        existing.setPhone(address.getPhone());
        // If setting this address as default, unset other defaults first
        if (address.isDefault()) {
            List<Address> existingList = addressRepository.findByUserId(user.getId());
            for (Address a : existingList) {
                if (a.isDefault() && !a.getId().equals(id)) {
                    a.setDefault(false);
                }
            }
            addressRepository.saveAll(existingList);
        }
        existing.setDefault(address.isDefault());
        Address saved = addressRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        Address existing = addressRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) return ResponseEntity.status(404).build();
        addressRepository.delete(existing);
        return ResponseEntity.ok().build();
    }
}
