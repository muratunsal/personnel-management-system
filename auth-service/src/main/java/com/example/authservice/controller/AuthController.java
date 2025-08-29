package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.ProvisionRequest;
import com.example.authservice.dto.ProvisionResponse;
import com.example.authservice.dto.UpdateUserRequest;
import com.example.authservice.model.User;
import com.example.authservice.model.UserRole;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.event.UserProvisionedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.authservice.service.AuthService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static com.example.authservice.config.RabbitMQConfig.USER_PROVISIONED_EXCHANGE;
import static com.example.authservice.config.RabbitMQConfig.USER_PROVISIONED_ROUTING_KEY;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for email {}", loginRequest.getEmail());
        Optional<String> token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (token.isPresent()) {
            String role = authService.getRoleFromToken(token.get());
            java.util.List<String> roles = authService.getJwtService().getRolesFromToken(token.get());
            LoginResponse response = new LoginResponse(token.get(), loginRequest.getEmail(), role, roles);
            log.info("Login success for email {} as {}", loginRequest.getEmail(), role);
            return ResponseEntity.ok(response);
        }
        
        log.warn("Login failed for email {}", loginRequest.getEmail());
        return ResponseEntity.badRequest().body("Invalid email or password");
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        log.info("Token validation requested");
        if (authService.validateToken(token)) {
            String email = authService.getEmailFromToken(token);
            String role = authService.getRoleFromToken(token);
            java.util.List<String> roles = authService.getJwtService().getRolesFromToken(token);
            log.info("Token valid for {} with role {}", email, role);
            return ResponseEntity.ok(new LoginResponse(token, email, role, roles));
        }
        
        log.warn("Token validation failed");
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @PostMapping("/provision")
    public ResponseEntity<?> provision(@Valid @RequestBody ProvisionRequest request) {
        log.info("Provision request for {}", request.getEmail());
        if ("admin".equalsIgnoreCase(request.getEmail())) {
            log.warn("Provision attempt blocked for admin user");
            return ResponseEntity.badRequest().body("Cannot provision admin via API");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.info("User already exists {}", request.getEmail());
            return ResponseEntity.ok(new ProvisionResponse(request.getEmail(), null, false));
        }
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : java.util.UUID.randomUUID().toString().substring(0, 10);
        User u = new User();
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(rawPassword));
        java.util.List<UserRole> roles = new java.util.ArrayList<>();
        for (String r : request.getRoles()) {
            roles.add(UserRole.valueOf(r));
        }
        u.setRoles(roles);
        userRepository.save(u);
        UserProvisionedEvent event = new UserProvisionedEvent();
        event.setEmail(request.getEmail());
        event.setFullName(null);
        event.setPassword(rawPassword);
        rabbitTemplate.convertAndSend(USER_PROVISIONED_EXCHANGE, USER_PROVISIONED_ROUTING_KEY, event,
                m -> { m.getMessageProperties().setHeader("__TypeId__", "com.example.authservice.event.UserProvisionedEvent"); return m; });
        log.info("User provisioned and event sent for {}", request.getEmail());
        return ResponseEntity.ok(new ProvisionResponse(request.getEmail(), rawPassword, true));
    }

    @PostMapping("/update-user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Update user request for {}", request.getEmail());
        if ("admin".equalsIgnoreCase(request.getEmail())) {
            log.warn("Update attempt blocked for admin user");
            return ResponseEntity.badRequest().body("Cannot update admin via API");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            log.warn("User not found {}", request.getEmail());
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
        log.info("User updated {}", user.getEmail());
        return ResponseEntity.ok().build();
    }
}
