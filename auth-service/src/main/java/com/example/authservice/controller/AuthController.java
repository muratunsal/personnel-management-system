package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.ProvisionRequest;
import com.example.authservice.dto.ProvisionResponse;
import com.example.authservice.dto.UpdateUserRequest;
import com.example.authservice.model.User;
import com.example.authservice.model.UserRole;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<String> token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (token.isPresent()) {
            String role = authService.getRoleFromToken(token.get());
            java.util.List<String> roles = authService.getJwtService().getRolesFromToken(token.get());
            LoginResponse response = new LoginResponse(token.get(), loginRequest.getEmail(), role, roles);
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.badRequest().body("Invalid email or password");
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        if (authService.validateToken(token)) {
            String email = authService.getEmailFromToken(token);
            String role = authService.getRoleFromToken(token);
            java.util.List<String> roles = authService.getJwtService().getRolesFromToken(token);
            return ResponseEntity.ok(new LoginResponse(token, email, role, roles));
        }
        
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @PostMapping("/provision")
    public ResponseEntity<?> provision(@Valid @RequestBody ProvisionRequest request) {
        if ("admin".equalsIgnoreCase(request.getEmail())) {
            return ResponseEntity.badRequest().body("Cannot provision admin via API");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.ok(new ProvisionResponse(request.getEmail(), null, false));
        }
        String rawPassword = java.util.UUID.randomUUID().toString().substring(0, 10);
        User u = new User();
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(rawPassword));
        java.util.List<UserRole> roles = new java.util.ArrayList<>();
        for (String r : request.getRoles()) {
            roles.add(UserRole.valueOf(r));
        }
        u.setRoles(roles);
        userRepository.save(u);
        return ResponseEntity.ok(new ProvisionResponse(request.getEmail(), rawPassword, true));
    }

    @PostMapping("/update-user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        if ("admin".equalsIgnoreCase(request.getEmail())) {
            return ResponseEntity.badRequest().body("Cannot update admin via API");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if (request.getNewEmail() != null && !request.getNewEmail().isBlank()) {
            user.setEmail(request.getNewEmail());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            java.util.List<UserRole> roles = new java.util.ArrayList<>();
            for (String r : request.getRoles()) roles.add(UserRole.valueOf(r));
            user.setRoles(roles);
        }
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
