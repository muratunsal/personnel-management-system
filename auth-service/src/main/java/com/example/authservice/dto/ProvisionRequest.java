package com.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ProvisionRequest {

    @Email
    @NotBlank
    private String email;

    @NotEmpty
    private List<String> roles;

    private String password;

    private Boolean suppressEmail;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getSuppressEmail() { return suppressEmail; }
    public void setSuppressEmail(Boolean suppressEmail) { this.suppressEmail = suppressEmail; }
}


