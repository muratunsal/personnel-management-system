package com.example.personnelservice.config;

import com.example.personnelservice.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (authService.validateToken(token)) {
                    String role = authService.extractRole(token);
                    java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null && !role.isEmpty()) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
                    }
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken("user", null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                }
            } catch (Exception e) {
            }
        } else {
        }
        
        filterChain.doFilter(request, response);
    }
}
