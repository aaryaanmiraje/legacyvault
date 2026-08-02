package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.service.BeneficiaryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService) {

        this.beneficiaryService = beneficiaryService;
    }

    // CREATE
    @PostMapping
    public Beneficiary createBeneficiary(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String relationship) {

        String ownerEmail = authentication.getName();

        return beneficiaryService.createBeneficiary(
                ownerEmail,
                name,
                email,
                relationship
        );
    }

    // GET ALL
    @GetMapping
    public List<Beneficiary> getBeneficiaries(
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return beneficiaryService.getBeneficiaries(ownerEmail);
    }

    // GET ONE
    @GetMapping("/{id}")
    public Beneficiary getBeneficiaryById(
            @PathVariable Long id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return beneficiaryService.getBeneficiaryById(
                id,
                ownerEmail
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable Long id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        beneficiaryService.deleteBeneficiary(
                id,
                ownerEmail
        );

        return ResponseEntity.noContent().build();
    }
}