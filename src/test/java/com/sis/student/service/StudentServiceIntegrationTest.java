package com.sis.student.service;

import com.sis.common.DatabaseManager;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class StudentServiceIntegrationTest {
    private final StudentService service = new StudentService();

    @BeforeEach
    void cleanup() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments WHERE student_id = 1");
        }
    }

    @Test
    void testIntegrity_QuotaCalculation_Relative() {
        // Section 1 is Software Engineering in your data.sql
        String result = service.addCourse(1L, 1L);
        assertTrue(result.contains("Successfully enrolled in"));
    }

    @Test
    void testFlow_ReEnrollAfterDrop_Solid() {
        // 1. Initial Enroll
        service.addCourse(1L, 1L);
        // 2. Drop
        service.dropCourse(1L, 1L);
        // 3. Re-enroll (Should trigger the UPDATE logic now, not INSERT)
        String result = service.addCourse(1L, 1L);
        assertTrue(result.contains("Successfully enrolled in"), "Service must handle the transition from DROPPED to ENROLLED.");
    }
}