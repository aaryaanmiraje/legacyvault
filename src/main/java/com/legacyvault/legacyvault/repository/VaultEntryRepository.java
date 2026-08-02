package com.legacyvault.legacyvault.repository;

import com.legacyvault.legacyvault.model.VaultEntry;
import com.legacyvault.legacyvault.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {

    // Get all vault entries belonging to a user
    List<VaultEntry> findByOwner(User owner);

    // Get one vault entry only if it belongs to this email
    Optional<VaultEntry> findByIdAndOwnerEmail(Long id, String email);
}