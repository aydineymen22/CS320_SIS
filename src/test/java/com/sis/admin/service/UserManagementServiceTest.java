package com.sis.admin.service;

import com.sis.admin.validation.UserValidator;
import com.sis.admin.validation.SecurityService;
import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserManagementServiceTest {

    private UserManagementService userService;
    private final String TEST_EMAIL = "integration_test_user@ozu.edu.tr";
    private final String INVALID_ROLE_EMAIL = "invalid_role_user@ozu.edu.tr";

    @BeforeEach
    void setUp() throws SQLException {
        UserValidator validator = new UserValidator();
        SecurityService securityService = new SecurityService();
        userService = new UserManagementService(validator, securityService);

        cleanupTestUser();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestUser();
    }

    private void cleanupTestUser() throws SQLException {
        String query = "DELETE FROM users WHERE email IN (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, TEST_EMAIL);
            stmt.setString(2, INVALID_ROLE_EMAIL);
            stmt.executeUpdate();
        }
    }

    @Test
    void testCreateUser_ValidSubmission_And_DuplicatePrevention() {
        String result = userService.createUser("STUDENT", "Integration", "Tester", TEST_EMAIL);

        assertTrue(result.startsWith("Success"), "User creation failed: " + result);

        String duplicateResult = userService.createUser("STUDENT", "Integration", "Tester", TEST_EMAIL);

        assertTrue(duplicateResult.startsWith("Error"));
        assertTrue(duplicateResult.contains("already exists"));
    }

    @Test
    void testCreateUser_InvalidRole_ReturnsErrorAndDoesNotInsertUser() throws SQLException {
        String result = userService.createUser("ADVISOR", "Invalid", "Role", INVALID_ROLE_EMAIL);

        assertTrue(result.startsWith("Error"));
        assertFalse(userExists(INVALID_ROLE_EMAIL), "Invalid role should not leave a base users row behind.");
    }

    @Test
    void testCreateUser_ValidRoles_CreateMatchingRoleRows() throws SQLException {
        assertRoleRowCreated("STUDENT", "students");
        cleanupTestUser();

        assertRoleRowCreated("INSTRUCTOR", "instructors");
        cleanupTestUser();

        assertRoleRowCreated("ADMIN", "admins");
    }

    @Test
    void testResetPassword_StoresBCryptVerifiableHash() throws SQLException {
        Long userId = createTestUserAndReturnId();

        String result = userService.resetPassword(userId);

        assertTrue(result.startsWith("Success"));
        String passwordHash = findPasswordHash(userId);
        assertTrue(new SecurityService().verifyPassword("ResetPassword123!", passwordHash));
    }

    @Test
    void testDeactivateUser_SetsIsActiveFalse() throws SQLException {
        Long userId = createTestUserAndReturnId();

        String result = userService.deactivateUser(userId);

        assertTrue(result.startsWith("Success"));
        assertFalse(isUserActive(userId));
    }

    private void assertRoleRowCreated(String role, String roleTable) throws SQLException {
        Long userId = createTestUserAndReturnId(role);
        String query = "SELECT COUNT(*) FROM " + roleTable + " WHERE " +
                ("students".equals(roleTable) ? "student_id" : "instructors".equals(roleTable) ? "instructor_id" : "admin_id") +
                " = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected role count row.");
            assertEquals(1, rs.getInt(1), "Expected " + role + " role row to be created.");
        }
    }

    private Long createTestUserAndReturnId() throws SQLException {
        return createTestUserAndReturnId("STUDENT");
    }

    private Long createTestUserAndReturnId(String role) throws SQLException {
        String result = userService.createUser(role, "Integration", "Tester", TEST_EMAIL);
        assertTrue(result.startsWith("Success"), "User creation failed: " + result);
        return findUserId(TEST_EMAIL);
    }

    private boolean userExists(String email) throws SQLException {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private Long findUserId(String email) throws SQLException {
        String query = "SELECT user_id FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected test user to exist.");
            return rs.getLong("user_id");
        }
    }

    private String findPasswordHash(Long userId) throws SQLException {
        String query = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected user password row.");
            return rs.getString("password_hash");
        }
    }

    private boolean isUserActive(Long userId) throws SQLException {
        String query = "SELECT is_active FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected user status row.");
            return rs.getBoolean("is_active");
        }
    }
}
