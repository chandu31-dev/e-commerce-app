package com.catchy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catchy.model.User;
import com.catchy.model.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser(User user);
    Optional<Vendor> findByShopName(String shopName);
    List<Vendor> findByApproved(boolean approved);
    List<Vendor> findByKycStatus(Vendor.KycStatus status);
}
