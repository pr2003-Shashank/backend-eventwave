package com.eventwave.service;

import com.eventwave.config.JwtService;
import com.eventwave.dto.LoginRequest;
import com.eventwave.dto.LoginResponse;
import com.eventwave.dto.UserDTO;
import com.eventwave.model.Role;
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

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        else if (!user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(request.getRole()))) {
        	throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user.getEmail());
        
        String matchedRole = user.getRoles().stream()
                .filter(role -> role.getName().equalsIgnoreCase(request.getRole()))
                .map(Role::getName)
                .findFirst()
                .orElse("UNKNOWN");

        UserDTO userDTO = new UserDTO();
        userDTO.setFullName(user.getFullName());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(matchedRole);

        return new LoginResponse("success", "Login successful", token, userDTO);
    }
}
