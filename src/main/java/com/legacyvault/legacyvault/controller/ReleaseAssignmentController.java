package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.ReleaseAssignment;
import com.legacyvault.legacyvault.service.ReleaseAssignmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class ReleaseAssignmentController {

    private final ReleaseAssignmentService releaseAssignmentService;

    public ReleaseAssignmentController(
            ReleaseAssignmentService releaseAssignmentService) {

        this.releaseAssignmentService = releaseAssignmentService;
    }

    // CREATE ASSIGNMENT
    @PostMapping
    public ReleaseAssignment createAssignment(
            Authentication authentication,
            @RequestParam Long vaultEntryId,
            @RequestParam Long beneficiaryId) {

        String ownerEmail = authentication.getName();

        return releaseAssignmentService.createAssignment(
                vaultEntryId,
                beneficiaryId,
                ownerEmail
        );
    }

    // GET ASSIGNMENTS FOR A VAULT ENTRY
    @GetMapping("/vault/{vaultEntryId}")
    public List<ReleaseAssignment> getAssignmentsForVaultEntry(
            @PathVariable Long vaultEntryId,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return releaseAssignmentService.getAssignmentsForVaultEntry(
                vaultEntryId,
                ownerEmail
        );
    }

    // DELETE ASSIGNMENT
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        releaseAssignmentService.deleteAssignment(
                assignmentId,
                ownerEmail
        );

        return ResponseEntity.noContent().build();
    }
}