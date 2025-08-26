package com.example.personnelservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final WebClient webClient;

    @Value("${auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("Token is null or empty");
            return false;
        }

        try {
            log.debug("Validating token with auth service: {}", authServiceUrl);
            
            Boolean isValid = webClient.post()
                    .uri(authServiceUrl + "/auth/validate")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("token=" + token)
                    .retrieve()
                    .onStatus(status -> status.is2xxSuccessful(), response -> Mono.empty())
                    .bodyToMono(Object.class)
                    .map(response -> {
                        log.debug("Token validation successful");
                        return true;
                    })
                    .onErrorReturn(false)
                    .block();
            
            boolean result = Boolean.TRUE.equals(isValid);
            log.debug("Token validation result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
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
            log.error("Error extracting role: {}", e.getMessage(), e);
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
            log.error("Error provisioning user {}: {}", email, e.getMessage());
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
            log.error("Error updating user {}: {}", email, e.getMessage());
            return false;
        }
    }
}
