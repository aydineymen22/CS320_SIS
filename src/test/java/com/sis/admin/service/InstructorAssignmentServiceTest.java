package com.sis.admin.service;

import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructorAssignmentServiceTest {

    private InstructorAssignmentService assignmentService;

    private Long testInstructorId;
    private Long replacementInstructorId;
    private Long testSectionId;

    private final String TEST_EMAIL = "temp_assignment_instructor@ozu.edu.tr";
    private final String REPLACEMENT_EMAIL = "temp_replacement_instructor@ozu.edu.tr";
    private final String TEST_COURSE_CODE = "TEST-ASSIGN-101";

    @BeforeEach
    void setUp() throws SQLException {
        assignmentService = new InstructorAssignmentService();
        cleanupTestData();
        setupPrerequisiteData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
    }

    private void setupPrerequisiteData() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {

            PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (email, password_hash, first_name, last_name) VALUES (?, 'hash', 'Temp', 'Inst')",
                    Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, TEST_EMAIL);
            userStmt.executeUpdate();
            ResultSet userRs = userStmt.getGeneratedKeys();
            userRs.next();
            testInstructorId = userRs.getLong(1);

            try (PreparedStatement instStmt = conn.prepareStatement("INSERT INTO instructors (instructor_id, title) VALUES (?, 'Dr.')")) {
                instStmt.setLong(1, testInstructorId);
                instStmt.executeUpdate();
            }

            PreparedStatement catalogStmt = conn.prepareStatement(
                    "INSERT INTO course_catalog (course_code, course_name) VALUES (?, 'Assignment Test Course')",
                    Statement.RETURN_GENERATED_KEYS);
            catalogStmt.setString(1, TEST_COURSE_CODE);
            catalogStmt.executeUpdate();
            ResultSet catalogRs = catalogStmt.getGeneratedKeys();
            catalogRs.next();
            long catalogId = catalogRs.getLong(1);

            try (PreparedStatement sectionStmt = conn.prepareStatement(
                    "INSERT INTO course_sections (catalog_course_id, term_id, section_no, quota, is_open) VALUES (?, 1, '1', 30, TRUE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                sectionStmt.setLong(1, catalogId);
                sectionStmt.executeUpdate();
                ResultSet sectionRs = sectionStmt.getGeneratedKeys();
                sectionRs.next();
                testSectionId = sectionRs.getLong(1);
            }

            PreparedStatement replacementUserStmt = conn.prepareStatement(
                    "INSERT INTO users (email, password_hash, first_name, last_name) VALUES (?, 'hash', 'Replace', 'Inst')",
                    Statement.RETURN_GENERATED_KEYS);
            replacementUserStmt.setString(1, REPLACEMENT_EMAIL);
            replacementUserStmt.executeUpdate();
            ResultSet replacementUserRs = replacementUserStmt.getGeneratedKeys();
            replacementUserRs.next();
            replacementInstructorId = replacementUserRs.getLong(1);

            try (PreparedStatement instStmt = conn.prepareStatement("INSERT INTO instructors (instructor_id, title) VALUES (?, 'Dr.')")) {
                instStmt.setLong(1, replacementInstructorId);
                instStmt.executeUpdate();
            }
        }
    }

    private void cleanupTestData() throws SQLException {
        String deleteSectionQuery = "DELETE FROM course_sections WHERE catalog_course_id IN (SELECT catalog_course_id FROM course_catalog WHERE course_code = ?)";
        String deleteCatalogQuery = "DELETE FROM course_catalog WHERE course_code = ?";
        String deleteUserQuery = "DELETE FROM users WHERE email IN (?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteSectionQuery)) {
                stmt1.setString(1, TEST_COURSE_CODE);
                stmt1.executeUpdate();
            }
            try (PreparedStatement stmt2 = conn.prepareStatement(deleteCatalogQuery)) {
                stmt2.setString(1, TEST_COURSE_CODE);
                stmt2.executeUpdate();
            }
            try (PreparedStatement stmt3 = conn.prepareStatement(deleteUserQuery)) {
                stmt3.setString(1, TEST_EMAIL);
                stmt3.setString(2, REPLACEMENT_EMAIL);
                stmt3.executeUpdate();
            }
        }
    }

    @Test
    void testAssignInstructor_WithValidIds_ReturnsSuccess() {
        String result = assignmentService.assignInstructor(testSectionId, testInstructorId);

        assertTrue(result.startsWith("Success"), "Expected assignment to succeed, but got: " + result);
    }

    @Test
    void testAssignInstructor_WithInvalidInstructor_ReturnsError() {
        String result = assignmentService.assignInstructor(testSectionId, 999999L);

        assertTrue(result.startsWith("Error"), "System should reject assignment for a non-existent instructor.");
    }

    @Test
    void testRemoveInstructor_WhenAssigned_ReturnsSuccess() {
        assignmentService.assignInstructor(testSectionId, testInstructorId);

        String result = assignmentService.removeInstructor(testSectionId);

        assertTrue(result.startsWith("Success"), "Expected removal to succeed, but got: " + result);
    }

    @Test
    void testAssignInstructor_WhenAlreadyAssigned_ReplacesInstructor() throws SQLException {
        assignmentService.assignInstructor(testSectionId, testInstructorId);

        String result = assignmentService.assignInstructor(testSectionId, replacementInstructorId);

        assertTrue(result.startsWith("Success"), "Expected replacement assignment to succeed, but got: " + result);
        assertEquals(replacementInstructorId, findAssignedInstructorId());
        assertEquals(1, countAssignmentsForSection());
    }

    private Long findAssignedInstructorId() throws SQLException {
        String query = "SELECT instructor_id FROM section_instructors WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, testSectionId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected instructor assignment to exist.");
            return rs.getLong("instructor_id");
        }
    }

    private int countAssignmentsForSection() throws SQLException {
        String query = "SELECT COUNT(*) FROM section_instructors WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, testSectionId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected assignment count row.");
            return rs.getInt(1);
        }
    }
}
