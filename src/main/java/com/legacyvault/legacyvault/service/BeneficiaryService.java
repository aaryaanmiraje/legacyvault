package com.legacyvault.legacyvault.service;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.User;
import com.legacyvault.legacyvault.repository.BeneficiaryRepository;
import com.legacyvault.legacyvault.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            UserRepository userRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    public Beneficiary createBeneficiary(
            String ownerEmail,
            String name,
            String email,
            String relationship) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        if (beneficiaryRepository.existsByOwnerAndEmail(owner, email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Beneficiary already exists"
            );
        }

        Beneficiary beneficiary = new Beneficiary(
                name,
                email,
                relationship,
                owner
        );

        return beneficiaryRepository.save(beneficiary);
    }

    // GET ALL
    public List<Beneficiary> getBeneficiaries(String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        return beneficiaryRepository.findByOwner(owner);
    }

    // GET ONE
    public Beneficiary getBeneficiaryById(
            Long id,
            String ownerEmail) {

        return beneficiaryRepository
                .findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beneficiary not found"
                ));
    }

    // DELETE
    public void deleteBeneficiary(
            Long id,
            String ownerEmail) {

        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beneficiary not found"
                ));

        beneficiaryRepository.delete(beneficiary);
    }
}