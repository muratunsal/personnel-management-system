package com.example.authservice.dto;

public class ProvisionResponse {
    private String email;
    private String password;
    private boolean created;

    public ProvisionResponse(String email, String password, boolean created) {
        this.email = email;
        this.password = password;
        this.created = created;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isCreated() { return created; }
    public void setCreated(boolean created) { this.created = created; }
}


