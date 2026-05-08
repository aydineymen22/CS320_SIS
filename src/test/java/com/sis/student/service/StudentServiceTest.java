package com.sis.student.service;

import com.sis.common.DatabaseManager;
import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {
    private final StudentService service = new StudentService();

    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments WHERE student_id = 1");
        }
    }

    @Test void testViewAvailable_NotEmpty() { assertFalse(service.viewAvailableCourses().isEmpty()); }
    @Test void testSearch_ValidQuery() { assertFalse(service.searchCourses("CS").isEmpty()); }
    @Test void testSearch_InvalidQuery() { assertTrue(service.searchCourses("INVALID_PROMPT_123").isEmpty()); }
    @Test void testAddCourse_Success() { assertEquals("Successfully enrolled.", service.addCourse(1L, 1L)); }
    @Test void testAddCourse_AlreadyEnrolled() {
        service.addCourse(1L, 1L);
        assertEquals("Error: Already enrolled in this section.", service.addCourse(1L, 1L));
    }
    @Test void testAddCourse_InvalidSection() { assertEquals("Error: Section not found.", service.addCourse(1L, 9999L)); }
    @Test void testDropCourse_Success() {
        service.addCourse(1L, 1L);
        assertEquals("Successfully dropped.", service.dropCourse(1L, 1L));
    }
    @Test void testDropCourse_NotFound() { assertEquals("Error: Enrollment not found.", service.dropCourse(1L, 888L)); }
    @Test void testViewTranscript_StudentIdMatch() {
        Transcript t = service.viewTranscript(1L);
        assertEquals(1L, t.getStudentId());
    }
    @Test void testAddCourse_ReEnrollAfterDrop() {
        service.addCourse(1L, 1L);
        service.dropCourse(1L, 1L);
        assertEquals("Successfully enrolled.", service.addCourse(1L, 1L)); // Should update DROPPED to ENROLLED
    }
}