package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.BeneficiaryVerificationToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryVerificationTokenRepository
        extends JpaRepository<BeneficiaryVerificationToken, Long> {

    Optional<BeneficiaryVerificationToken> findByTokenHash(
            String tokenHash
    );

    Optional<BeneficiaryVerificationToken> findByBeneficiary(
            Beneficiary beneficiary
    );

    void deleteByBeneficiary(
            Beneficiary beneficiary
    );
}