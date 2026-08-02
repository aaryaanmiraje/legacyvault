package com.legacyvault.legacyvault;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.ReleaseAssignment;
import com.legacyvault.legacyvault.model.ReleaseRequest;
import com.legacyvault.legacyvault.model.ReleaseStatus;

import com.legacyvault.legacyvault.repository.BeneficiaryRepository;
import com.legacyvault.legacyvault.repository.ReleaseAssignmentRepository;
import com.legacyvault.legacyvault.repository.ReleaseRequestRepository;

import com.legacyvault.legacyvault.service.BeneficiaryAccessService;
import com.legacyvault.legacyvault.service.ReleaseRequestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReleaseRequestServiceTest {

    private ReleaseRequestRepository releaseRequestRepository;
    private BeneficiaryRepository beneficiaryRepository;
    private ReleaseAssignmentRepository releaseAssignmentRepository;
    private BeneficiaryAccessService beneficiaryAccessService;

    private ReleaseRequestService releaseRequestService;

    @BeforeEach
    void setUp() {

        releaseRequestRepository =
                mock(ReleaseRequestRepository.class);

        beneficiaryRepository =
                mock(BeneficiaryRepository.class);

        releaseAssignmentRepository =
                mock(ReleaseAssignmentRepository.class);

        beneficiaryAccessService =
                mock(BeneficiaryAccessService.class);

        releaseRequestService =
                new ReleaseRequestService(
                        releaseRequestRepository,
                        beneficiaryRepository,
                        releaseAssignmentRepository,
                        beneficiaryAccessService
                );
    }


    // TEST 1
    // Verified beneficiary can have a release request created
    @Test
    void shouldCreateRequestForVerifiedBeneficiary() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(beneficiaryRepository.findByIdAndOwnerEmail(
                1L,
                "owner@example.com"
        )).thenReturn(Optional.of(beneficiary));

        when(releaseRequestRepository
                .existsByBeneficiaryAndStatusIn(
                        eq(beneficiary),
                        anyList()
                ))
                .thenReturn(false);

        when(releaseRequestRepository
                .save(any(ReleaseRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ReleaseRequest request =
                releaseRequestService.createRequest(
                        1L,
                        "owner@example.com"
                );

        assertNotNull(request);

        verify(releaseRequestRepository)
                .save(any(ReleaseRequest.class));
    }


    // TEST 2
    // Unverified beneficiary cannot have a release request
    @Test
    void shouldRejectRequestForUnverifiedBeneficiary() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        when(beneficiary.isVerified())
                .thenReturn(false);

        when(beneficiaryRepository.findByIdAndOwnerEmail(
                1L,
                "owner@example.com"
        )).thenReturn(Optional.of(beneficiary));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.createRequest(
                                        1L,
                                        "owner@example.com"
                                )
                );

        assertEquals(
                400,
                exception.getStatusCode().value()
        );

        verify(
                releaseRequestRepository,
                never()
        ).save(any());
    }


    // TEST 3
    // Duplicate active release requests must be rejected
    @Test
    void shouldRejectDuplicateActiveRequest() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(beneficiaryRepository.findByIdAndOwnerEmail(
                1L,
                "owner@example.com"
        )).thenReturn(Optional.of(beneficiary));

        when(releaseRequestRepository
                .existsByBeneficiaryAndStatusIn(
                        eq(beneficiary),
                        anyList()
                ))
                .thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.createRequest(
                                        1L,
                                        "owner@example.com"
                                )
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(
                releaseRequestRepository,
                never()
        ).save(any());
    }


    // TEST 4
    // PENDING -> UNDER_REVIEW
    @Test
    void shouldStartReviewFromPending() {

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.PENDING);

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(releaseRequestRepository.save(request))
                .thenReturn(request);

        ReleaseRequest result =
                releaseRequestService.startReview(1L);

        assertSame(request, result);

        verify(request)
                .setStatus(ReleaseStatus.UNDER_REVIEW);

        verify(request)
                .setReviewedAt(any(LocalDateTime.class));

        verify(releaseRequestRepository)
                .save(request);
    }


    // TEST 5
    // Cannot start review from an invalid state
    @Test
    void shouldRejectInvalidStartReviewTransition() {

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.APPROVED);

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.startReview(1L)
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(
                releaseRequestRepository,
                never()
        ).save(any());
    }


    // TEST 6
    // UNDER_REVIEW -> APPROVED
    @Test
    void shouldApproveRequestUnderReview() {

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.UNDER_REVIEW);

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(releaseRequestRepository.save(request))
                .thenReturn(request);

        ReleaseRequest result =
                releaseRequestService.approveRequest(
                        1L,
                        "Approved"
                );

        assertSame(request, result);

        verify(request)
                .setStatus(ReleaseStatus.APPROVED);

        verify(request)
                .setApprovedAt(any(LocalDateTime.class));

        verify(request)
                .setReleaseEligibleAt(any(LocalDateTime.class));

        verify(request)
                .setReviewNotes("Approved");

        verify(releaseRequestRepository)
                .save(request);
    }


    // TEST 7
    // PENDING request can be rejected
    @Test
    void shouldRejectPendingRequest() {

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.PENDING);

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(releaseRequestRepository.save(request))
                .thenReturn(request);

        ReleaseRequest result =
                releaseRequestService.rejectRequest(
                        1L,
                        "Insufficient evidence"
                );

        assertSame(request, result);

        verify(request)
                .setStatus(ReleaseStatus.REJECTED);

        verify(request)
                .setReviewedAt(any(LocalDateTime.class));

        verify(request)
                .setReviewNotes("Insufficient evidence");

        verify(releaseRequestRepository)
                .save(request);
    }


    // TEST 8
    // APPROVED request cannot be rejected
    @Test
    void shouldRejectInvalidRejectionTransition() {

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.APPROVED);

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.rejectRequest(
                                        1L,
                                        "Rejected"
                                )
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );
    }


    // TEST 9
    // Cooling-off period must be enforced
    @Test
    void shouldRejectReleaseBeforeCoolingOffPeriodEnds() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.APPROVED);

        when(request.getBeneficiary())
                .thenReturn(beneficiary);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(request.getReleaseEligibleAt())
                .thenReturn(
                        LocalDateTime.now()
                                .plusHours(1)
                );

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.releaseRequest(1L)
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(
                beneficiaryAccessService,
                never()
        ).generateAccessToken(any());
    }


    // TEST 10
    // Release must fail when beneficiary has no assignments
    @Test
    void shouldRejectReleaseWithNoAssignments() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.APPROVED);

        when(request.getBeneficiary())
                .thenReturn(beneficiary);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(beneficiary.getId())
                .thenReturn(1L);

        when(request.getReleaseEligibleAt())
                .thenReturn(
                        LocalDateTime.now()
                                .minusMinutes(1)
                );

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(releaseAssignmentRepository
                .findByBeneficiaryId(1L))
                .thenReturn(List.of());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                releaseRequestService.releaseRequest(1L)
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(
                beneficiaryAccessService,
                never()
        ).generateAccessToken(any());
    }


    // TEST 11
    // Successful release should release assignments
    // and generate beneficiary access token
    @Test
    void shouldSuccessfullyReleaseApprovedRequest() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        ReleaseRequest request =
                mock(ReleaseRequest.class);

        ReleaseAssignment assignment =
                mock(ReleaseAssignment.class);

        when(request.getStatus())
                .thenReturn(ReleaseStatus.APPROVED);

        when(request.getBeneficiary())
                .thenReturn(beneficiary);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(beneficiary.getId())
                .thenReturn(1L);

        when(request.getReleaseEligibleAt())
                .thenReturn(
                        LocalDateTime.now()
                                .minusMinutes(1)
                );

        when(releaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(releaseAssignmentRepository
                .findByBeneficiaryId(1L))
                .thenReturn(List.of(assignment));

        when(beneficiaryAccessService
                .generateAccessToken(beneficiary))
                .thenReturn("TEST_ACCESS_TOKEN");

        String token =
                releaseRequestService.releaseRequest(1L);

        assertEquals(
                "TEST_ACCESS_TOKEN",
                token
        );

        verify(assignment)
                .setReleased(true);

        verify(releaseAssignmentRepository)
                .saveAll(anyList());

        verify(request)
                .setStatus(ReleaseStatus.RELEASED);

        verify(request)
                .setReleasedAt(any(LocalDateTime.class));

        verify(releaseRequestRepository)
                .save(request);

        verify(beneficiaryAccessService)
                .generateAccessToken(beneficiary);
    }
}