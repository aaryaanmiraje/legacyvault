package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.VaultEntry;
import com.legacyvault.legacyvault.service.VaultService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    // CREATE
    @PostMapping
    public VaultEntry createEntry(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String username,
            @RequestParam(required = false) String website,
            @RequestParam String password,
            @RequestParam(required = false) String notes) {

        String email = authentication.getName();

        return vaultService.createEntry(
                email,
                title,
                username,
                website,
                password,
                notes
        );
    }

    // GET ALL
    @GetMapping
    public List<VaultEntry> getEntries(
            Authentication authentication) {

        String email = authentication.getName();

        return vaultService.getEntries(email);
    }

    // GET ONE
    @GetMapping("/{id}")
    public VaultEntry getEntryById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        return vaultService.getEntryById(id, email);
    }

    // UPDATE
    @PutMapping("/{id}")
    public VaultEntry updateEntry(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String username,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String notes) {

        String email = authentication.getName();

        return vaultService.updateEntry(
                id,
                email,
                title,
                username,
                website,
                password,
                notes
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        vaultService.deleteEntry(id, email);

        return ResponseEntity.noContent().build();
    }

    // REVEAL PASSWORD
    @PostMapping("/{id}/reveal")
    public Map<String, String> revealPassword(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam String accountPassword) {

        String email = authentication.getName();

        String password = vaultService.revealPassword(
                id,
                email,
                accountPassword
        );

        return Map.of("password", password);
    }
}