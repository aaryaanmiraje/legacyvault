package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.ReleaseRequest;
import com.legacyvault.legacyvault.service.ReleaseRequestService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/release-requests")
public class AdminReleaseController {

    private final ReleaseRequestService releaseRequestService;

    public AdminReleaseController(
            ReleaseRequestService releaseRequestService) {

        this.releaseRequestService = releaseRequestService;
    }

    // PENDING -> UNDER_REVIEW
    @PostMapping("/{requestId}/start-review")
    public ReleaseRequest startReview(
            @PathVariable Long requestId) {

        return releaseRequestService.startReview(requestId);
    }


    // UNDER_REVIEW -> APPROVED
    @PostMapping("/{requestId}/approve")
    public ReleaseRequest approveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reviewNotes) {

        return releaseRequestService.approveRequest(
                requestId,
                reviewNotes
        );
    }


    // PENDING / UNDER_REVIEW -> REJECTED
    @PostMapping("/{requestId}/reject")
    public ReleaseRequest rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reviewNotes) {

        return releaseRequestService.rejectRequest(
                requestId,
                reviewNotes
        );
    }


    // APPROVED -> RELEASED
    @PostMapping("/{requestId}/release")
    public Map<String, String> releaseRequest(
            @PathVariable Long requestId) {

        String accessToken =
                releaseRequestService.releaseRequest(
                        requestId
                );

        /*
         * DEVELOPMENT ONLY:
         *
         * Eventually this token must be emailed directly
         * to the beneficiary and must NOT be returned
         * to the administrator.
         */
        return Map.of(
                "message", "Release completed",
                "beneficiaryAccessToken", accessToken
        );
    }
}