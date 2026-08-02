package com.legacyvault.legacyvault.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "release_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"vault_entry_id", "beneficiary_id"}
        )
    }
)
public class ReleaseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Secret/item that will eventually be released
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_entry_id", nullable = false)
    private VaultEntry vaultEntry;

    // Person who will receive it
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Column(nullable = false)
    private boolean released = false;

    // Required by JPA
    public ReleaseAssignment() {
    }

    public ReleaseAssignment(
            VaultEntry vaultEntry,
            Beneficiary beneficiary) {

        this.vaultEntry = vaultEntry;
        this.beneficiary = beneficiary;
        this.released = false;
    }

    public Long getId() {
        return id;
    }

    public VaultEntry getVaultEntry() {
        return vaultEntry;
    }

    public void setVaultEntry(VaultEntry vaultEntry) {
        this.vaultEntry = vaultEntry;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}