package com.legacyvault.legacyvault.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiary_verification_tokens")
public class BeneficiaryVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SHA-256 hash of the real verification token
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "beneficiary_id",
            nullable = false,
            unique = true
    )
    private Beneficiary beneficiary;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    // Required by JPA
    public BeneficiaryVerificationToken() {
    }

    public BeneficiaryVerificationToken(
            String tokenHash,
            Beneficiary beneficiary,
            LocalDateTime expiresAt) {

        this.tokenHash = tokenHash;
        this.beneficiary = beneficiary;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public Long getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}