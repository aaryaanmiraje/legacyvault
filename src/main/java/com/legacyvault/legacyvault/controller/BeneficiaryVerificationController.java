package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.service.BeneficiaryVerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/beneficiary-verification")
public class BeneficiaryVerificationController {

    private final BeneficiaryVerificationService verificationService;

    public BeneficiaryVerificationController(
            BeneficiaryVerificationService verificationService) {

        this.verificationService = verificationService;
    }

    // Generate invitation token
    // TEMPORARY: returns token so we can test locally
    @PostMapping("/generate/{beneficiaryId}")
    public Map<String, String> generateToken(
            @PathVariable Long beneficiaryId,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        String token =
                verificationService.generateVerificationToken(
                        beneficiaryId,
                        ownerEmail
                );

        return Map.of("token", token);
    }

    // Verify beneficiary using invitation token
    @PostMapping("/verify")
    public ResponseEntity<String> verifyBeneficiary(
            @RequestParam String token) {

        verificationService.verifyBeneficiary(token);

        return ResponseEntity.ok(
                "Beneficiary verified successfully"
        );
    }
}