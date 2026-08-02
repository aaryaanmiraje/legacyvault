package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {

    // Get all beneficiaries belonging to this user
    List<Beneficiary> findByOwner(User owner);

    // Get one beneficiary only if it belongs to this user
    Optional<Beneficiary> findByIdAndOwnerEmail(
            Long id,
            String email
    );

    // Check whether this user already added this email
    boolean existsByOwnerAndEmail(
            User owner,
            String email
    );
}