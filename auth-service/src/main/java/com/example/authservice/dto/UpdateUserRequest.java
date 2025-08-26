package com.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class UpdateUserRequest {
    @Email
    @NotBlank
    private String email;

    private String newEmail;

    private List<String> roles;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}


