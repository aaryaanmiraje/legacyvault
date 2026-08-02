package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.ReleaseRequest;
import com.legacyvault.legacyvault.model.ReleaseStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReleaseRequestRepository
        extends JpaRepository<ReleaseRequest, Long> {

    List<ReleaseRequest> findByBeneficiary(
            Beneficiary beneficiary
    );

    boolean existsByBeneficiaryAndStatusIn(
            Beneficiary beneficiary,
            List<ReleaseStatus> statuses
    );

    Optional<ReleaseRequest> findByIdAndBeneficiary(
            Long id,
            Beneficiary beneficiary
    );

    // Get all release requests belonging to beneficiaries
    // created by this owner
    List<ReleaseRequest> findByBeneficiaryOwnerEmail(
            String ownerEmail
    );
}