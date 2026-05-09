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
        if (isBlank(role) || isBlank(firstName) || isBlank(lastName) || isBlank(email)) {
            return "Error: Role, Name, and Email are required.";
        }

        String normalizedRole = role.trim().toUpperCase();
        if (!isSupportedRole(normalizedRole)) {
            return "Error: Invalid role. Supported roles are STUDENT, INSTRUCTOR, and ADMIN.";
        }

        if (!userValidator.isUniqueEmail(email)) {
            return "Error: A user with the email '" + email + "' already exists.";
        }

        String tempPassword = "DefaultPassword123!";
        String hashedPassword = securityService.hashPassword(tempPassword);

        try {
            Long newUserId = userRepository.saveUser(normalizedRole, firstName, lastName, email, hashedPassword);
            return "Success: " + normalizedRole + " account created for " + firstName + " " + lastName + " (User ID: " + newUserId + ").";
        } catch (SQLException e) {
            return "Database Error: Could not create user. " + e.getMessage();
        }
    }

    public String updateUser(Long userId, String firstName, String lastName) {
        if (userId == null || isBlank(firstName) || isBlank(lastName)) {
            return "Error: Invalid user update parameters.";
        }

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

    private boolean isSupportedRole(String role) {
        return "STUDENT".equals(role) || "INSTRUCTOR".equals(role) || "ADMIN".equals(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
