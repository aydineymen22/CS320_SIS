package com.sis.student.repository;

import com.sis.common.DatabaseManager;
import com.sis.common.model.Course;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentRepositoryTest {
    private final StudentRepository repository = new StudentRepository();

    @BeforeEach
    void clean() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments WHERE student_id = 1 AND section_id = 1");
        }
    }

    @Test @Order(1) void testFindCourses_NotNull() throws SQLException { assertNotNull(repository.findCourses("")); }
    @Test @Order(2) void testFindCourses_SearchMatch() throws SQLException {
        List<Course> res = repository.findCourses("CS");
        assertTrue(res.stream().anyMatch(c -> c.getCourseCode().contains("CS")));
    }
    @Test @Order(3) void testFindCourses_EmptyOnNoMatch() throws SQLException { assertTrue(repository.findCourses("NON_EXISTENT").isEmpty()); }
    @Test @Order(4) void testInsertEnrollment_Success() throws SQLException {
        repository.insertEnrollment(1L, 1L);
        assertEquals("ENROLLED", repository.getEnrollmentStatus(1L, 1L));
    }
    @Test @Order(5) void testGetEnrollmentStatus_NullForNew() throws SQLException { assertNull(repository.getEnrollmentStatus(1L, 999L)); }
    @Test @Order(6) void testUpdateStatus_ToDropped() throws SQLException {
        repository.insertEnrollment(1L, 1L);
        repository.updateEnrollmentStatus(1L, 1L, "DROPPED");
        assertEquals("DROPPED", repository.getEnrollmentStatus(1L, 1L));
    }
    @Test @Order(7) void testUpdateStatus_ToEnrolled() throws SQLException {
        repository.insertEnrollment(1L, 1L);
        repository.updateEnrollmentStatus(1L, 1L, "ENROLLED");
        assertEquals("ENROLLED", repository.getEnrollmentStatus(1L, 1L));
    }
    @Test @Order(8) void testTranscriptData_NotNull() throws SQLException { assertNotNull(repository.getTranscriptData(1L)); }
    @Test @Order(9) void testTranscriptData_ResultSize() throws SQLException {
        ResultSet rs = repository.getTranscriptData(1L);
        assertNotNull(rs);
    }
    @Test @Order(10) void testDatabaseConnection() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertFalse(conn.isClosed());
        }
    }
}