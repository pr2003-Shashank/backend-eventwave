package com.eventwave.service;

import com.eventwave.dto.RegistrationRequest;
import com.eventwave.dto.UserProfileUpdateRequest;
import com.eventwave.model.Role;
import com.eventwave.model.User;
import com.eventwave.repository.RoleRepository;
import com.eventwave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.eventwave.exception.EmailAlreadyExistsException;


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
    	    throw new EmailAlreadyExistsException("Email already registered.");
    	}

        // Auto-generate a unique username (e.g., based on email prefix + UUID suffix)
        String emailPrefix = request.getEmail().split("@")[0];
        String generatedUsername = emailPrefix + "_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(generatedUsername);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Assign role
        String roleName = request.getRole().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRoles(Set.of(role));

        userRepository.save(user);
        return "User registered successfully";
    }

    public String updateUserProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBio(request.getBio());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setZipCode(request.getZipCode());

        if (request.getLatitude() != null)
            user.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null)
            user.setLongitude(BigDecimal.valueOf(request.getLongitude()));

        userRepository.save(user);
        return "Profile updated successfully";
    }

}
