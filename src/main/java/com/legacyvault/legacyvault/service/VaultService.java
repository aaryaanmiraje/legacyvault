package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.User;
import com.legacyvault.legacyvault.model.VaultEntry;
import com.legacyvault.legacyvault.repository.UserRepository;
import com.legacyvault.legacyvault.repository.VaultEntryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VaultService {

    private final VaultEntryRepository vaultEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final BCryptPasswordEncoder passwordEncoder;

    public VaultService(
            VaultEntryRepository vaultEntryRepository,
            UserRepository userRepository,
            EncryptionService encryptionService) {

        this.vaultEntryRepository = vaultEntryRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // CREATE
    public VaultEntry createEntry(
            String email,
            String title,
            String username,
            String website,
            String password,
            String notes) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        String encryptedPassword =
                encryptionService.encrypt(password);

        VaultEntry entry = new VaultEntry(
                title,
                username,
                website,
                encryptedPassword,
                notes,
                user
        );

        return vaultEntryRepository.save(entry);
    }

    // GET ALL
    public List<VaultEntry> getEntries(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        return vaultEntryRepository.findByOwner(user);
    }

    // GET ONE
    public VaultEntry getEntryById(Long id, String email) {

        return vaultEntryRepository
                .findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));
    }

    // UPDATE
    public VaultEntry updateEntry(
            Long id,
            String email,
            String title,
            String username,
            String website,
            String password,
            String notes) {

        VaultEntry entry = vaultEntryRepository
                .findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));

        entry.setTitle(title);
        entry.setUsername(username);
        entry.setWebsite(website);
        entry.setNotes(notes);

        if (password != null && !password.isBlank()) {

            String encryptedPassword =
                    encryptionService.encrypt(password);

            entry.setEncryptedPassword(encryptedPassword);
        }

        return vaultEntryRepository.save(entry);
    }

    // DELETE
    public void deleteEntry(Long id, String email) {

        VaultEntry entry = vaultEntryRepository
                .findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));

        vaultEntryRepository.delete(entry);
    }

    // REVEAL PASSWORD
    public String revealPassword(
            Long id,
            String email,
            String accountPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        // Re-check the user's account password
        if (!passwordEncoder.matches(
                accountPassword,
                user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        // Make sure this vault entry belongs to this user
        VaultEntry entry = vaultEntryRepository
                .findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vault entry not found"
                ));

        // Decrypt only after authentication + ownership checks
        return encryptionService.decrypt(
                entry.getEncryptedPassword()
        );
    }
}