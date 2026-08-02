package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.dto.ReleasedVaultEntry;
import com.legacyvault.legacyvault.model.ReleaseAssignment;
import com.legacyvault.legacyvault.model.VaultEntry;
import com.legacyvault.legacyvault.repository.ReleaseAssignmentRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BeneficiaryVaultService {

    private final ReleaseAssignmentRepository releaseAssignmentRepository;
    private final EncryptionService encryptionService;

    public BeneficiaryVaultService(
            ReleaseAssignmentRepository releaseAssignmentRepository,
            EncryptionService encryptionService) {

        this.releaseAssignmentRepository =
                releaseAssignmentRepository;

        this.encryptionService =
                encryptionService;
    }


    public List<ReleasedVaultEntry> getReleasedEntries(
            Long beneficiaryId) {

        // Only assignments belonging to this beneficiary
        List<ReleaseAssignment> assignments =
                releaseAssignmentRepository
                        .findByBeneficiaryId(beneficiaryId);

        List<ReleasedVaultEntry> releasedEntries =
                new ArrayList<>();

        for (ReleaseAssignment assignment : assignments) {

            // Never expose unreleased assignments
            if (!assignment.isReleased()) {
                continue;
            }

            VaultEntry vaultEntry =
                    assignment.getVaultEntry();

            // Decrypt only after confirming assignment is released
            String decryptedPassword =
                    encryptionService.decrypt(
                            vaultEntry.getEncryptedPassword()
                    );

            ReleasedVaultEntry response =
                    new ReleasedVaultEntry(
                            vaultEntry.getId(),
                            vaultEntry.getTitle(),
                            vaultEntry.getUsername(),
                            vaultEntry.getWebsite(),
                            decryptedPassword,
                            vaultEntry.getNotes()
                    );

            releasedEntries.add(response);
        }

        return releasedEntries;
    }
}