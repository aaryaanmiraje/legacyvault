package com.legacyvault.legacyvault.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "release_requests")
public class ReleaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Beneficiary requesting the release
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    // Current state of the release request
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseStatus status;

    // When the release request was created
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // When an admin started reviewing the request
    private LocalDateTime reviewedAt;

    // When an admin approved the request
    private LocalDateTime approvedAt;

    // Earliest time at which release is allowed
    private LocalDateTime releaseEligibleAt;

    // When the secrets were actually released
    private LocalDateTime releasedAt;

    // Notes added during review
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;


    // Required by JPA
    public ReleaseRequest() {
    }


    // Used when creating a new release request
    public ReleaseRequest(Beneficiary beneficiary) {

        this.beneficiary = beneficiary;
        this.status = ReleaseStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }


    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }


    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }


    public ReleaseStatus getStatus() {
        return status;
    }

    public void setStatus(ReleaseStatus status) {
        this.status = status;
    }


    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }


    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }


    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }


    public LocalDateTime getReleaseEligibleAt() {
        return releaseEligibleAt;
    }

    public void setReleaseEligibleAt(
            LocalDateTime releaseEligibleAt) {

        this.releaseEligibleAt = releaseEligibleAt;
    }


    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }


    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}