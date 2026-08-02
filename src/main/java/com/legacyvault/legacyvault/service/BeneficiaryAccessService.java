package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.BeneficiaryAccessToken;
import com.legacyvault.legacyvault.repository.BeneficiaryAccessTokenRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class BeneficiaryAccessService {

    private final BeneficiaryAccessTokenRepository accessTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public BeneficiaryAccessService(
            BeneficiaryAccessTokenRepository accessTokenRepository) {

        this.accessTokenRepository = accessTokenRepository;
    }


    // GENERATE A NEW BENEFICIARY ACCESS TOKEN
    public String generateAccessToken(
            Beneficiary beneficiary) {

        if (!beneficiary.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Beneficiary must be verified"
            );
        }

        // Generate 32 cryptographically secure random bytes
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        // URL-safe token
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Store only SHA-256 hash in database
        String tokenHash = hashToken(rawToken);

        // Token valid for 30 minutes
        LocalDateTime expiresAt =
                LocalDateTime.now().plusMinutes(30);

        BeneficiaryAccessToken accessToken =
                new BeneficiaryAccessToken(
                        beneficiary,
                        tokenHash,
                        expiresAt
                );

        accessTokenRepository.save(accessToken);

        // Raw token is returned only once
        return rawToken;
    }


    // VALIDATE TOKEN
    public BeneficiaryAccessToken validateToken(
            String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Access token is required"
            );
        }

        String tokenHash =
                hashToken(rawToken);

        BeneficiaryAccessToken accessToken =
                accessTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid access token"
                                )
                        );

        if (accessToken.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Access token has already been used"
            );
        }

        if (LocalDateTime.now()
                .isAfter(accessToken.getExpiresAt())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Access token has expired"
            );
        }

        if (!accessToken
                .getBeneficiary()
                .isVerified()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Beneficiary is not verified"
            );
        }

        return accessToken;
    }


    // MARK TOKEN AS USED
    public void markTokenUsed(
            BeneficiaryAccessToken accessToken) {

        accessToken.setUsed(true);
        accessToken.setUsedAt(
                LocalDateTime.now()
        );

        accessTokenRepository.save(accessToken);
    }


    // SHA-256 HASH
    private String hashToken(String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    e
            );
        }
    }
}