package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.ReleaseAssignment;
import com.legacyvault.legacyvault.model.ReleaseRequest;
import com.legacyvault.legacyvault.model.ReleaseStatus;

import com.legacyvault.legacyvault.repository.BeneficiaryRepository;
import com.legacyvault.legacyvault.repository.ReleaseAssignmentRepository;
import com.legacyvault.legacyvault.repository.ReleaseRequestRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReleaseRequestService {

    private final ReleaseRequestRepository releaseRequestRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ReleaseAssignmentRepository releaseAssignmentRepository;
    private final BeneficiaryAccessService beneficiaryAccessService;

    public ReleaseRequestService(
            ReleaseRequestRepository releaseRequestRepository,
            BeneficiaryRepository beneficiaryRepository,
            ReleaseAssignmentRepository releaseAssignmentRepository,
            BeneficiaryAccessService beneficiaryAccessService) {

        this.releaseRequestRepository = releaseRequestRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.releaseAssignmentRepository = releaseAssignmentRepository;
        this.beneficiaryAccessService = beneficiaryAccessService;
    }


    // CREATE RELEASE REQUEST
    public ReleaseRequest createRequest(
            Long beneficiaryId,
            String ownerEmail) {

        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndOwnerEmail(beneficiaryId, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beneficiary not found"
                ));

        if (!beneficiary.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Beneficiary must be verified"
            );
        }

        List<ReleaseStatus> activeStatuses = List.of(
                ReleaseStatus.PENDING,
                ReleaseStatus.UNDER_REVIEW,
                ReleaseStatus.APPROVED
        );

        boolean activeRequestExists =
                releaseRequestRepository
                        .existsByBeneficiaryAndStatusIn(
                                beneficiary,
                                activeStatuses
                        );

        if (activeRequestExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active release request already exists"
            );
        }

        ReleaseRequest request =
                new ReleaseRequest(beneficiary);

        return releaseRequestRepository.save(request);
    }


    // GET OWNER'S RELEASE REQUESTS
    public List<ReleaseRequest> getRequestsForOwner(
            String ownerEmail) {

        return releaseRequestRepository
                .findByBeneficiaryOwnerEmail(ownerEmail);
    }


    // PENDING -> UNDER_REVIEW
    public ReleaseRequest startReview(Long requestId) {

        ReleaseRequest request = findRequest(requestId);

        requireStatus(
                request,
                ReleaseStatus.PENDING
        );

        request.setStatus(
                ReleaseStatus.UNDER_REVIEW
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        return releaseRequestRepository.save(request);
    }


    // UNDER_REVIEW -> APPROVED
    public ReleaseRequest approveRequest(
            Long requestId,
            String reviewNotes) {

        ReleaseRequest request = findRequest(requestId);

        requireStatus(
                request,
                ReleaseStatus.UNDER_REVIEW
        );

        LocalDateTime now =
                LocalDateTime.now();

        request.setStatus(
                ReleaseStatus.APPROVED
        );

        request.setApprovedAt(now);

        // 24-hour cooling-off period
        request.setReleaseEligibleAt(
                now.plusHours(24)
        );

        request.setReviewNotes(
                reviewNotes
        );

        return releaseRequestRepository.save(request);
    }


    // PENDING / UNDER_REVIEW -> REJECTED
    public ReleaseRequest rejectRequest(
            Long requestId,
            String reviewNotes) {

        ReleaseRequest request = findRequest(requestId);

        if (request.getStatus() != ReleaseStatus.PENDING
                && request.getStatus() != ReleaseStatus.UNDER_REVIEW) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Release request cannot be rejected from current status"
            );
        }

        request.setStatus(
                ReleaseStatus.REJECTED
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        request.setReviewNotes(
                reviewNotes
        );

        return releaseRequestRepository.save(request);
    }


    // APPROVED -> RELEASED
    @Transactional
    public String releaseRequest(Long requestId) {

        ReleaseRequest request =
                findRequest(requestId);

        // Must already be APPROVED
        requireStatus(
                request,
                ReleaseStatus.APPROVED
        );

        Beneficiary beneficiary =
                request.getBeneficiary();

        // Beneficiary must still be verified
        if (!beneficiary.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Beneficiary is not verified"
            );
        }

        LocalDateTime eligibleAt =
                request.getReleaseEligibleAt();

        if (eligibleAt == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Release eligibility time is missing"
            );
        }

        // Enforce cooling-off period
        if (LocalDateTime.now().isBefore(eligibleAt)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cooling-off period has not finished"
            );
        }

        // Get ONLY this beneficiary's assignments
        List<ReleaseAssignment> assignments =
                releaseAssignmentRepository
                        .findByBeneficiaryId(
                                beneficiary.getId()
                        );

        if (assignments.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No vault entries are assigned to this beneficiary"
            );
        }

        // Mark only their assignments as released
        for (ReleaseAssignment assignment : assignments) {
            assignment.setReleased(true);
        }

        releaseAssignmentRepository.saveAll(
                assignments
        );

        // Mark request released
        request.setStatus(
                ReleaseStatus.RELEASED
        );

        request.setReleasedAt(
                LocalDateTime.now()
        );

        releaseRequestRepository.save(request);

        /*
         * Generate beneficiary access token.
         *
         * TEMPORARY DEVELOPMENT BEHAVIOR:
         * We return the raw token so we can test it.
         *
         * In production this must be emailed directly
         * to the beneficiary and never shown to admin.
         */
        return beneficiaryAccessService
                .generateAccessToken(beneficiary);
    }


    private ReleaseRequest findRequest(
            Long requestId) {

        return releaseRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Release request not found"
                        )
                );
    }


    private void requireStatus(
            ReleaseRequest request,
            ReleaseStatus requiredStatus) {

        if (request.getStatus() != requiredStatus) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid release request state transition"
            );
        }
    }
}