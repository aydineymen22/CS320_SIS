package com.sis.student.service;

import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {
    private final StudentService service = new StudentService();

    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments WHERE student_id IN (1, 2)");
        }
    }

    @Test void testAdd_Success_ReturnsFullMessage() {
        assertTrue(service.addCourse(1L, 1L).contains("Successfully enrolled in"));
    }

    @Test void testAdd_Duplicate_ReturnsScheduleError() {
        service.addCourse(1L, 1L);
        assertEquals("Error: Already in your schedule.", service.addCourse(1L, 1L));
    }

    @Test void testAdd_NonExistentSection() {
        assertEquals("Error: Course not found.", service.addCourse(1L, 9999L));
    }

    @Test void testAdd_ReEnrollAfterDrop_Logic() {
        service.addCourse(1L, 1L);
        service.dropCourse(1L, 1L);
        String res = service.addCourse(1L, 1L);
        assertTrue(res.contains("Successfully"), "Should handle update on dropped record");
    }

    @Test void testDrop_NormalFlow() {
        service.addCourse(1L, 1L);
        assertEquals("Successfully dropped.", service.dropCourse(1L, 1L));
    }

    @Test void testDrop_FailOnMissingRecord() {
        assertEquals("Error: Enrollment not found.", service.dropCourse(1L, 888L));
    }

    @Test void testSearch_CS320_Found() {
        assertFalse(service.searchCourses("CS320").isEmpty());
    }

    @Test void testGetSchedule_ReturnsOneRow() {
        service.addCourse(1L, 1L);
        assertEquals(1, service.getMySchedule(1L).size());
    }

    @Test void testViewAvailable_NotEmpty() {
        assertFalse(service.viewAvailableCourses().isEmpty());
    }

    @Test void testTranscript_Creation() {
        assertNotNull(service.viewTranscript(1L));
    }

    @Test void testAdd_MultipleStudents() {
        service.addCourse(1L, 1L);
        assertTrue(service.addCourse(2L, 1L).contains("Successfully"));
    }

    @Test void testSearch_EmptyStringReturnsAll() {
        assertEquals(service.viewAvailableCourses().size(), service.searchCourses("").size());
    }
}