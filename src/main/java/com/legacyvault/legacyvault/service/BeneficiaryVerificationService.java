package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.BeneficiaryVerificationToken;

import com.legacyvault.legacyvault.repository.BeneficiaryRepository;
import com.legacyvault.legacyvault.repository.BeneficiaryVerificationTokenRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class BeneficiaryVerificationService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryVerificationTokenRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public BeneficiaryVerificationService(
            BeneficiaryRepository beneficiaryRepository,
            BeneficiaryVerificationTokenRepository tokenRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.tokenRepository = tokenRepository;
    }

    // Generate a verification token
    @Transactional
    public String generateVerificationToken(
            Long beneficiaryId,
            String ownerEmail) {

        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndOwnerEmail(beneficiaryId, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beneficiary not found"
                ));

        if (beneficiary.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Beneficiary is already verified"
            );
        }

        // Remove an older verification token
        tokenRepository.deleteByBeneficiary(beneficiary);

        // Generate 32 random bytes
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        // URL-safe token that would eventually be emailed
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Only store its SHA-256 hash
        String tokenHash = hashToken(rawToken);

        BeneficiaryVerificationToken verificationToken =
                new BeneficiaryVerificationToken(
                        tokenHash,
                        beneficiary,
                        LocalDateTime.now().plusHours(24)
                );

        tokenRepository.save(verificationToken);

        // Raw token is returned only so we can test the flow locally.
        return rawToken;
    }

    // Verify beneficiary using token
    @Transactional
    public void verifyBeneficiary(String rawToken) {

        String tokenHash = hashToken(rawToken);

        BeneficiaryVerificationToken verificationToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid verification token"
                        ));

        if (verificationToken.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token has already been used"
            );
        }

        if (verificationToken.isExpired()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token has expired"
            );
        }

        Beneficiary beneficiary =
                verificationToken.getBeneficiary();

        beneficiary.setVerified(true);

        verificationToken.setUsed(true);

        beneficiaryRepository.save(beneficiary);
        tokenRepository.save(verificationToken);
    }

    // Convert raw token into SHA-256 hash
    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not hash verification token",
                    e
            );
        }
    }
}