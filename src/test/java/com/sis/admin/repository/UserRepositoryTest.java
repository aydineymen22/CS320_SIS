package com.sis.admin.repository;

import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    private UserRepository repository;
    private final String TEST_EMAIL = "repo_test_user@ozu.edu.tr";

    @BeforeEach
    void setUp() throws SQLException {
        repository = new UserRepository();
        cleanupTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
    }

    private void cleanupTestData() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.prepareStatement("DELETE FROM users WHERE email = '" + TEST_EMAIL + "'").executeUpdate();
        }
    }

    @Test
    void testSaveUser_StudentRole_And_Exists() throws SQLException {
        assertFalse(repository.existsByEmail(TEST_EMAIL));

        Long userId = repository.saveUser("STUDENT", "Repo", "Tester", TEST_EMAIL, "hashed_pw");

        assertNotNull(userId);
        assertTrue(userId > 0);
        assertTrue(repository.existsByEmail(TEST_EMAIL));
    }

    @Test
    void testSaveUser_AdminRole() throws SQLException {
        Long userId = repository.saveUser("ADMIN", "Admin", "Tester", TEST_EMAIL, "hashed_pw");
        assertNotNull(userId);
    }

    @Test
    void testUpdateUserDetails() throws SQLException {
        Long userId = repository.saveUser("INSTRUCTOR", "OldName", "OldLast", TEST_EMAIL, "hashed_pw");

        boolean isUpdated = repository.updateUserDetails(userId, "NewName", "NewLast");
        assertTrue(isUpdated, "Repository should return true when user details are updated.");
    }

    @Test
    void testUpdatePassword_And_DeactivateUser() throws SQLException {
        Long userId = repository.saveUser("STUDENT", "Security", "Tester", TEST_EMAIL, "old_hash");

        boolean passwordUpdated = repository.updatePassword(userId, "new_secure_hash");
        assertTrue(passwordUpdated, "Password update SQL should execute successfully.");

        boolean statusUpdated = repository.setStatus(userId, false);
        assertTrue(statusUpdated, "Status update SQL should execute successfully.");
    }
}
