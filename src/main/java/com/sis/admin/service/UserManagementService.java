package com.sis.admin.service;

import com.sis.admin.repository.UserRepository;
import com.sis.admin.validation.SecurityService;
import com.sis.admin.validation.UserValidator;
import java.sql.SQLException;

public class UserManagementService {
    private final UserValidator userValidator;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    public UserManagementService(UserValidator userValidator, SecurityService securityService) {
        this.userValidator = userValidator;
        this.securityService = securityService;
        this.userRepository = new UserRepository();
    }

    public String createUser(String role, String firstName, String lastName, String email) {
        if (!userValidator.isUniqueEmail(email)) {
            return "Error: A user with the email '" + email + "' already exists.";
        }

        String tempPassword = "DefaultPassword123!";
        String hashedPassword = securityService.hashPassword(tempPassword);

        try {
            Long newUserId = userRepository.saveUser(role, firstName, lastName, email, hashedPassword);
            return "Success: " + role + " account created for " + firstName + " " + lastName + " (User ID: " + newUserId + ").";
        } catch (SQLException e) {
            return "Database Error: Could not create user. " + e.getMessage();
        }
    }

    public String updateUser(Long userId, String firstName, String lastName) {
        boolean success = userRepository.updateUserDetails(userId, firstName, lastName);
        return success ? "Success: User ID " + userId + " details updated." : "Error: User ID not found or update failed.";
    }

    public String deactivateUser(Long userId) {
        boolean success = userRepository.setStatus(userId, false);
        return success ? "Success: User ID " + userId + " has been deactivated." : "Error: User ID not found.";
    }

    public String resetPassword(Long userId) {
        String newTempPassword = "ResetPassword123!";
        String newHashedPassword = securityService.hashPassword(newTempPassword);

        boolean success = userRepository.updatePassword(userId, newHashedPassword);
        return success ? "Success: Password reset for User ID " + userId + ". Temporary password generated." : "Error: User ID not found.";
    }
}