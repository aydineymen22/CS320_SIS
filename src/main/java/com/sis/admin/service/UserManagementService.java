package com.sis.admin.service;

import com.sis.admin.validation.SecurityService;
import com.sis.admin.validation.UserValidator;

public class UserManagementService {
    private final UserValidator userValidator;
    private final SecurityService securityService;

    // Temporary mock data counter for generating IDs
    private Long userIdCounter = 3000L;

    public UserManagementService(UserValidator userValidator, SecurityService securityService) {
        this.userValidator = userValidator;
        this.securityService = securityService;
    }

    public String createUser(String role, String firstName, String lastName, String email) {
        if (!userValidator.isUniqueEmail(email)) {
            return "Error: A user with the email '" + email + "' already exists.";
        }

        // Generate a temporary password for new users
        String tempPassword = "DefaultPassword123!";
        String hashedPassword = securityService.hashPassword(tempPassword);

        Long newUserId = userIdCounter++;

        // TODO: Build User entity with hashedPassword and save to UserRepository
        return "Success: " + role + " account created for " + firstName + " " + lastName + " (ID: " + newUserId + ").";
    }

    public String updateUser(Long userId, String firstName, String lastName) {
        // TODO: Fetch user, update names, save
        return "Success: User ID " + userId + " has been updated.";
    }

    public String deactivateUser(Long userId) {
        // TODO: Fetch user, set status to inactive, save
        return "Success: User ID " + userId + " has been deactivated. They can no longer log in.";
    }

    public String resetPassword(Long userId) {
        // TODO: Generate new temp password, hash via SecurityService, save
        return "Success: Password reset for User ID " + userId + ". Temporary password generated.";
    }
}