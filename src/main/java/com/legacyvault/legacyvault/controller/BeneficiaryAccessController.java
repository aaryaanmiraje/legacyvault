package com.legacyvault.legacyvault.controller;

import com.legacyvault.legacyvault.dto.ReleasedVaultEntry;
import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.BeneficiaryAccessToken;
import com.legacyvault.legacyvault.service.BeneficiaryAccessService;
import com.legacyvault.legacyvault.service.BeneficiaryVaultService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/beneficiary-access")
public class BeneficiaryAccessController {

    private final BeneficiaryAccessService beneficiaryAccessService;
    private final BeneficiaryVaultService beneficiaryVaultService;

    public BeneficiaryAccessController(
            BeneficiaryAccessService beneficiaryAccessService,
            BeneficiaryVaultService beneficiaryVaultService) {

        this.beneficiaryAccessService =
                beneficiaryAccessService;

        this.beneficiaryVaultService =
                beneficiaryVaultService;
    }


    // REDEEM ONE-TIME ACCESS TOKEN
    @PostMapping("/redeem")
    public Map<String, Object> redeemToken(
            @RequestParam String token,
            HttpServletRequest request) {

        BeneficiaryAccessToken accessToken =
                beneficiaryAccessService
                        .validateToken(token);

        Beneficiary beneficiary =
                accessToken.getBeneficiary();


        // Prevent session fixation
        HttpSession oldSession =
                request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }


        // Create new beneficiary session
        HttpSession session =
                request.getSession(true);

        session.setAttribute(
                "BENEFICIARY_ID",
                beneficiary.getId()
        );

        session.setAttribute(
                "BENEFICIARY_EMAIL",
                beneficiary.getEmail()
        );


        // One-time token cannot be redeemed again
        beneficiaryAccessService
                .markTokenUsed(accessToken);


        return Map.of(
                "message",
                "Beneficiary access granted",

                "beneficiaryId",
                beneficiary.getId(),

                "beneficiaryEmail",
                beneficiary.getEmail()
        );
    }


    // GET RELEASED VAULT ENTRIES
    @GetMapping("/vault")
    public List<ReleasedVaultEntry> getReleasedVault(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        // Beneficiary must have redeemed a valid token
        if (session == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Beneficiary session required"
            );
        }


        Object beneficiaryIdObject =
                session.getAttribute(
                        "BENEFICIARY_ID"
                );

        if (beneficiaryIdObject == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Beneficiary session required"
            );
        }


        Long beneficiaryId =
                (Long) beneficiaryIdObject;


        return beneficiaryVaultService
                .getReleasedEntries(
                        beneficiaryId
                );
    }
}