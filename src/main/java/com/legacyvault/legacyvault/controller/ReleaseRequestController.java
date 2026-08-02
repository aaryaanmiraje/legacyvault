package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.ReleaseRequest;
import com.legacyvault.legacyvault.service.ReleaseRequestService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/release-requests")
public class ReleaseRequestController {

    private final ReleaseRequestService releaseRequestService;

    public ReleaseRequestController(
            ReleaseRequestService releaseRequestService) {

        this.releaseRequestService = releaseRequestService;
    }

    // CREATE RELEASE REQUEST
    @PostMapping
    public ReleaseRequest createRequest(
            @RequestParam Long beneficiaryId,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return releaseRequestService.createRequest(
                beneficiaryId,
                ownerEmail
        );
    }

    // GET ALL RELEASE REQUESTS FOR LOGGED-IN OWNER
    @GetMapping
    public List<ReleaseRequest> getRequests(
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return releaseRequestService
                .getRequestsForOwner(ownerEmail);
    }
}