package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.BeneficiaryAccessToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryAccessTokenRepository
        extends JpaRepository<BeneficiaryAccessToken, Long> {

    // Find an access token using its SHA-256 hash
    Optional<BeneficiaryAccessToken> findByTokenHash(
            String tokenHash
    );
}