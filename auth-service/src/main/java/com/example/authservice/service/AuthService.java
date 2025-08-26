package com.example.authservice.service;

import com.example.authservice.model.User;
import com.example.authservice.model.UserRole;
import com.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public Optional<String> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            user.get().setLastLoginAt(LocalDateTime.now());
            userRepository.save(user.get());
            UserRole primary = choosePrimaryRole(user.get().getRoles());
            return Optional.of(jwtService.generateToken(email, primary, user.get().getRoles()));
        }
        
        return Optional.empty();
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtService.getEmailFromToken(token);
    }

    public String getRoleFromToken(String token) { return jwtService.getRoleFromToken(token); }

    public JwtService getJwtService() { return jwtService; }

    private UserRole choosePrimaryRole(java.util.List<UserRole> roles) {
        if (roles == null || roles.isEmpty()) return UserRole.EMPLOYEE;
        if (roles.contains(UserRole.ADMIN)) return UserRole.ADMIN;
        if (roles.contains(UserRole.HR)) return UserRole.HR;
        if (roles.contains(UserRole.HEAD)) return UserRole.HEAD;
        return UserRole.EMPLOYEE;
    }
}
