package com.example.authservice.dto;

public class LoginResponse {
    
    private String token;
    private String email;
    private String role;
    private java.util.List<String> roles;

    public LoginResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public LoginResponse(String token, String email, String role, java.util.List<String> roles) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public java.util.List<String> getRoles() { return roles; }
    public void setRoles(java.util.List<String> roles) { this.roles = roles; }
}
