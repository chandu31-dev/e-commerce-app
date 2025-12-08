package com.catchy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.Vendor;
import com.catchy.model.VendorProduct;

@Repository
public interface VendorProductRepository extends JpaRepository<VendorProduct, Long> {
    List<VendorProduct> findByVendor(Vendor vendor);
    List<VendorProduct> findByVendorAndActive(Vendor vendor, boolean active);
    Optional<VendorProduct> findByVendorAndProductId(Vendor vendor, Long productId);
    List<VendorProduct> findByProductId(Long productId);
}
