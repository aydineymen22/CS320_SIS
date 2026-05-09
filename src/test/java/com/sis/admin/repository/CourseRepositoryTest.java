package com.sis.admin.repository;

import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CourseRepositoryTest {

    private CourseRepository repository;
    private final String TEST_COURSE_CODE = "REPO-101";
    private final String TEST_COURSE_NAME = "Repository Testing";
    private Long tempInstructorId;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new CourseRepository();
        cleanupTestData();
        setupTemporaryInstructor();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
    }

    private void setupTemporaryInstructor() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (email, password_hash, first_name, last_name) VALUES ('repo_inst@ozu.edu.tr', 'hash', 'Inst', 'Repo')",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                tempInstructorId = rs.getLong(1);
                PreparedStatement instStmt = conn.prepareStatement("INSERT INTO instructors (instructor_id, title) VALUES (?, 'Dr.')");
                instStmt.setLong(1, tempInstructorId);
                instStmt.executeUpdate();
            }
        }
    }

    private void cleanupTestData() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.prepareStatement("DELETE FROM course_sections WHERE catalog_course_id IN (SELECT catalog_course_id FROM course_catalog WHERE course_code = '" + TEST_COURSE_CODE + "')").executeUpdate();
            conn.prepareStatement("DELETE FROM course_catalog WHERE course_code = '" + TEST_COURSE_CODE + "'").executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE email = 'repo_inst@ozu.edu.tr'").executeUpdate();
        }
    }

    @Test
    void testSaveCourse_And_Exists() throws SQLException {

        assertFalse(repository.existsByCodeOrName(TEST_COURSE_CODE, TEST_COURSE_NAME));

        Long sectionId = repository.saveCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 50);

        assertNotNull(sectionId);
        assertTrue(sectionId > 0);

        assertTrue(repository.existsByCodeOrName(TEST_COURSE_CODE, TEST_COURSE_NAME));
    }

    @Test
    void testUpdateCourseQuota_ReturnsTrueOnSuccess() throws SQLException {
        Long sectionId = repository.saveCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 50);

        boolean isUpdated = repository.updateCourseQuota(sectionId, 100);
        assertTrue(isUpdated, "Repository should return true when a row is successfully updated.");
    }

    @Test
    void testUpdateCourseDetails_UpdatesNameAndQuota() throws SQLException {
        Long sectionId = repository.saveCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 50);

        boolean isUpdated = repository.updateCourseDetails(sectionId, "Repository Testing Updated", 75);

        assertTrue(isUpdated, "Repository should update both course name and quota.");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT cc.course_name, cs.quota FROM course_sections cs " +
                             "JOIN course_catalog cc ON cs.catalog_course_id = cc.catalog_course_id " +
                             "WHERE cs.section_id = ?")) {
            stmt.setLong(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Expected updated course row.");
            assertEquals("Repository Testing Updated", rs.getString("course_name"));
            assertEquals(75, rs.getInt("quota"));
        }
    }

    @Test
    void testInstructorAssignment_AssignAndRemove() throws SQLException {
        Long sectionId = repository.saveCourse(TEST_COURSE_CODE, TEST_COURSE_NAME, 50);

        boolean assigned = repository.assignInstructor(sectionId, tempInstructorId);
        assertTrue(assigned, "Repository should successfully insert into section_instructors.");

        boolean removed = repository.removeInstructor(sectionId);
        assertTrue(removed, "Repository should successfully delete from section_instructors.");
    }
}
