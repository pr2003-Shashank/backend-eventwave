package com.eventwave.dto;

public class AttendeeDTO {
    private Long userId;
    private String email;
    private String fullName;

    public AttendeeDTO(Long userId, String email, String fullName) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
