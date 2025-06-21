package com.eventwave.service;

import com.eventwave.dto.RegistrationRequest;
import com.eventwave.model.Role;
import com.eventwave.model.User;
import com.eventwave.repository.RoleRepository;
import com.eventwave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class UserService {

	@Autowired
	private RoleRepository roleRepository;
	
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setZipCode(request.getZipCode());

        if (request.getLatitude() != null)
            user.setLatitude(BigDecimal.valueOf(request.getLatitude()));

        if (request.getLongitude() != null)
            user.setLongitude(BigDecimal.valueOf(request.getLongitude()));
         
     // Assign role
        String roleName = request.getRole() != null ? request.getRole().toUpperCase() : "USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRoles(Set.of(role));
        
        userRepository.save(user);
        return "User registered successfully";
    }
}
