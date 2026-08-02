package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.ReleaseAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseAssignmentRepository
        extends JpaRepository<ReleaseAssignment, Long> {

    // Get assignments for a specific vault entry
    List<ReleaseAssignment> findByVaultEntryId(
            Long vaultEntryId
    );

    // Get ONLY assignments belonging to a beneficiary
    List<ReleaseAssignment> findByBeneficiaryId(
            Long beneficiaryId
    );

    // Prevent duplicate assignments
    boolean existsByVaultEntryIdAndBeneficiaryId(
            Long vaultEntryId,
            Long beneficiaryId
    );

    // Delete a particular assignment
    void deleteByVaultEntryIdAndBeneficiaryId(
            Long vaultEntryId,
            Long beneficiaryId
    );
}