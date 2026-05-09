package com.sis.admin.service;

import com.sis.admin.validation.CourseValidator;
import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseManagementServiceTest {

    private CourseManagementService courseService;

    // We use a highly specific test code so we know exactly what to delete later
    private final String TEST_COURSE_CODE = "TEST-999";
    private final String TEST_COURSE_NAME = "Integration Testing Fundamentals";
    private final String UPDATED_COURSE_NAME = "Updated Integration Testing Fundamentals";

    @BeforeEach
    void setUp() throws SQLException {
        CourseValidator validator = new CourseValidator();
        courseService = new CourseManagementService(validator);

        cleanupTestCourse();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestCourse();
    }

    private void cleanupTestCourse() throws SQLException {
        String deleteSectionQuery = "DELETE cs FROM course_sections cs " +
                "JOIN course_catalog cc ON cs.catalog_course_id = cc.catalog_course_id " +
                "WHERE cc.course_code = ? OR cc.course_name = ?";
        String deleteCatalogQuery = "DELETE FROM course_catalog WHERE course_code = ? OR course_name = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(deleteSectionQuery)) {
                stmt1.setString(1, TEST_COURSE_CODE);
                stmt1.setString(2, UPDATED_COURSE_NAME);
                stmt1.executeUpdate();
            }
            try (PreparedStatement stmt2 = conn.prepareStatement(deleteCatalogQuery)) {
                stmt2.setString(1, TEST_COURSE_CODE);
                stmt2.setString(2, UPDATED_COURSE_NAME);
                stmt2.executeUpdate();
            }
        }
    }

    @Test
    void testCreateCourse_WithValidData_ReturnsSuccess() {
        String result = courseService.createCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 40);

        assertTrue(result.startsWith("Success"), "Expected success message but got: " + result);
        assertTrue(result.contains("Section ID:"), "Expected message to contain the new Section ID.");
    }

    @Test
    void testCreateCourse_WithDuplicateCode_ReturnsError() {
        courseService.createCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 40);

        String duplicateResult = courseService.createCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 40);

        assertTrue(duplicateResult.startsWith("Error"));
        assertTrue(duplicateResult.contains("already exists"));
    }

    @Test
    void testUpdateCourseQuota_WithValidId_UpdatesSuccessfully() {
        String createMessage = courseService.createCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 40);

        String idString = createMessage.substring(createMessage.lastIndexOf(" ") + 1);
        Long sectionId = Long.parseLong(idString);

        String updateResult = courseService.updateCourse(sectionId, UPDATED_COURSE_NAME, 100);

        assertTrue(updateResult.startsWith("Success"));
        assertTrue(updateResult.contains("100"));
        assertCourseDetails(sectionId, UPDATED_COURSE_NAME, 100);
    }

    private void assertCourseDetails(Long sectionId, String expectedName, int expectedQuota) {
        String query = "SELECT cc.course_name, cs.quota " +
                "FROM course_sections cs " +
                "JOIN course_catalog cc ON cs.catalog_course_id = cc.catalog_course_id " +
                "WHERE cs.section_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "Expected updated course row to exist.");
            assertEquals(expectedName, rs.getString("course_name"));
            assertEquals(expectedQuota, rs.getInt("quota"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
