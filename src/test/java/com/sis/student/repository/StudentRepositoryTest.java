package com.sis.student.repository;

import com.sis.common.DatabaseManager;
import com.sis.common.model.Course;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryTest {
    private final StudentRepository repo = new StudentRepository();

    @BeforeEach
    void cleanup() throws SQLException {
        // Clear all possible test rows for both test students
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments WHERE student_id IN (1, 2)");
        }
    }

    @Test void testFindCourses_NotNull() throws SQLException { assertNotNull(repo.findCourses("")); }

    @Test void testFindCourses_CS320_DataIntegrity() throws SQLException {
        List<Course> results = repo.findCourses("CS320");
        assertTrue(results.stream().anyMatch(c -> c.getCourseCode().equals("CS320")));
    }

    @Test void testGetStatus_ReturnsNullForNewRecord() throws SQLException {
        assertNull(repo.getEnrollmentStatus(1L, 99L));
    }

    @Test void testInsertEnrollment_PersistenceCheck() throws SQLException {
        repo.insertEnrollment(1L, 1L);
        assertEquals("ENROLLED", repo.getEnrollmentStatus(1L, 1L));
    }

    @Test void testUpdateStatus_ToDropped() throws SQLException {
        // This was failing; now it has a clean slate
        repo.insertEnrollment(2L, 2L);
        repo.updateEnrollmentStatus(2L, 2L, "DROPPED");
        assertEquals("DROPPED", repo.getEnrollmentStatus(2L, 2L));
    }

    @Test void testFindEnrolled_MappingCorrectness() throws SQLException {
        repo.insertEnrollment(1L, 1L);
        assertFalse(repo.findEnrolledCourses(1L).isEmpty());
    }

    @Test void testTranscript_ResultSetIsTraversable() throws SQLException {
        try (ResultSet rs = repo.getTranscriptData(1L)) {
            assertNotNull(rs);
            assertFalse(rs.isClosed());
        }
    }

    @Test void testQuota_PositiveValue() throws SQLException {
        assertTrue(repo.findCourses("").get(0).getAvailableQuota() >= 0);
    }

    @Test void testSection_NotNullStrings() throws SQLException {
        assertNotNull(repo.findCourses("").get(0).getSectionNo());
    }

    @Test void testSearch_InvalidQueryReturnsEmpty() throws SQLException {
        assertTrue(repo.findCourses("XYZ_NON_EXISTENT_99").isEmpty());
    }

    @Test void testDatabase_IsConnectivityValid() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertTrue(conn.isValid(1));
        }
    }

    @Test void testInstructor_NamePresentInView() throws SQLException {
        assertNotNull(repo.findCourses("").get(0).getInstructorName());
    }
}