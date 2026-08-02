package com.legacyvault.legacyvault.dto;

public class ReleasedVaultEntry {

    private Long id;
    private String title;
    private String username;
    private String website;
    private String password;
    private String notes;

    public ReleasedVaultEntry(
            Long id,
            String title,
            String username,
            String website,
            String password,
            String notes) {

        this.id = id;
        this.title = title;
        this.username = username;
        this.website = website;
        this.password = password;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getWebsite() {
        return website;
    }

    public String getPassword() {
        return password;
    }

    public String getNotes() {
        return notes;
    }
}