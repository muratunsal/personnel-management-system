package com.example.personnelservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class AuthService {


    private final WebClient webClient;

    @Value("${auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Boolean isValid = webClient.post()
                    .uri(authServiceUrl + "/auth/validate")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("token=" + token)
                    .retrieve()
                    .onStatus(status -> status.is2xxSuccessful(), response -> Mono.empty())
                    .bodyToMono(Object.class)
                    .map(response -> {
                        return true;
                    })
                    .onErrorReturn(false)
                    .block();
            
            boolean result = Boolean.TRUE.equals(isValid);
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        try {
                    String role = webClient.post()
                .uri(authServiceUrl + "/auth/validate")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("token=" + token)
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .map(map -> (String) map.get("role"))
                .onErrorReturn("")
                .block();
            return role;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            String email = webClient.post()
                .uri(authServiceUrl + "/auth/validate")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("token=" + token)
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .map(map -> (String) map.get("email"))
                .onErrorReturn("")
                .block();
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    public String provision(String email, java.util.List<String> roles) {
        try {
            java.util.Map<String, Object> req = new java.util.HashMap<>();
            req.put("email", email);
            req.put("roles", roles);
            java.util.Map resp = webClient.post()
                    .uri(authServiceUrl + "/auth/provision")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();
            Object pw = resp != null ? resp.get("password") : null;
            return pw != null ? pw.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String provisionWithPassword(String email, java.util.List<String> roles, String password) {
        try {
            java.util.Map<String, Object> req = new java.util.HashMap<>();
            req.put("email", email);
            req.put("roles", roles);
            if (password != null && !password.isBlank()) req.put("password", password);
            java.util.Map resp = webClient.post()
                    .uri(authServiceUrl + "/auth/provision")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();
            Object pw = resp != null ? resp.get("password") : null;
            return pw != null ? pw.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updateUser(String email, String newEmail, java.util.List<String> roles) {
        try {
            java.util.Map<String, Object> req = new java.util.HashMap<>();
            req.put("email", email);
            if (newEmail != null) req.put("newEmail", newEmail);
            if (roles != null) req.put("roles", roles);
            webClient.post()
                    .uri(authServiceUrl + "/auth/update-user")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
