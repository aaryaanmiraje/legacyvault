package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.ReleaseAssignment;
import com.legacyvault.legacyvault.model.VaultEntry;

import com.legacyvault.legacyvault.repository.BeneficiaryRepository;
import com.legacyvault.legacyvault.repository.ReleaseAssignmentRepository;
import com.legacyvault.legacyvault.repository.VaultEntryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReleaseAssignmentService {

    private final ReleaseAssignmentRepository releaseAssignmentRepository;
    private final VaultEntryRepository vaultEntryRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    public ReleaseAssignmentService(
            ReleaseAssignmentRepository releaseAssignmentRepository,
            VaultEntryRepository vaultEntryRepository,
            BeneficiaryRepository beneficiaryRepository) {

        this.releaseAssignmentRepository = releaseAssignmentRepository;
        this.vaultEntryRepository = vaultEntryRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    // CREATE ASSIGNMENT
    public ReleaseAssignment createAssignment(
            Long vaultEntryId,
            Long beneficiaryId,
            String ownerEmail) {

        // Make sure the vault entry belongs to the logged-in user
        VaultEntry vaultEntry = vaultEntryRepository
                .findByIdAndOwnerEmail(vaultEntryId, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));

        // Make sure the beneficiary belongs to the logged-in user
        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndOwnerEmail(beneficiaryId, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beneficiary not found"
                ));

        // Beneficiary MUST be verified before assignment
        if (!beneficiary.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Beneficiary must be verified before assignment"
            );
        }

        // Prevent duplicate assignments
        if (releaseAssignmentRepository
                .existsByVaultEntryIdAndBeneficiaryId(
                        vaultEntryId,
                        beneficiaryId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assignment already exists"
            );
        }

        ReleaseAssignment assignment =
                new ReleaseAssignment(
                        vaultEntry,
                        beneficiary
                );

        return releaseAssignmentRepository.save(assignment);
    }

    // GET ALL ASSIGNMENTS FOR A VAULT ENTRY
    public List<ReleaseAssignment> getAssignmentsForVaultEntry(
            Long vaultEntryId,
            String ownerEmail) {

        // First verify that the vault belongs to this user
        vaultEntryRepository
                .findByIdAndOwnerEmail(vaultEntryId, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));

        return releaseAssignmentRepository
                .findByVaultEntryId(vaultEntryId);
    }

    // DELETE ASSIGNMENT
    public void deleteAssignment(
            Long assignmentId,
            String ownerEmail) {

        ReleaseAssignment assignment =
                releaseAssignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Assignment not found"
                        ));

        // Get the owner of the vault connected to this assignment
        String actualOwnerEmail =
                assignment
                        .getVaultEntry()
                        .getOwner()
                        .getEmail();

        // Prevent another user from deleting this assignment
        if (!actualOwnerEmail.equals(ownerEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Assignment not found"
            );
        }

        releaseAssignmentRepository.delete(assignment);
    }
}