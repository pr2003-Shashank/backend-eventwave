package com.eventwave.service;

import com.eventwave.config.JwtService;
import com.eventwave.dto.LoginRequest;
import com.eventwave.model.User;
import com.eventwave.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        else if (!user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(request.getRole()))) {
        	throw new RuntimeException("Invalid credentials");
        }
        
        return jwtService.generateToken(user.getEmail());
    }
}
