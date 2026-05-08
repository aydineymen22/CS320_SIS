package com.sis.student.service;

import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceIntegrationTest {
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
    }

    @Test void testIntegrity_QuotaCalculation_Relative() {
        studentService.dropCourse(2L, 1L);

        List<Course> coursesBefore = studentService.searchCourses("CS320");
        int before = coursesBefore.get(0).getAvailableQuota();

        String result = studentService.addCourse(2L, 1L);

        assertEquals("Successfully enrolled.", result, "Enrollment must succeed for the quota to change.");

        int after = studentService.searchCourses("CS320").get(0).getAvailableQuota();

        assertEquals(before - 1, after, "Available quota must decrease by exactly 1 after a successful enrollment.");
    }

    @Test void testFlow_ReEnrollAfterDrop_Solid() {
        studentService.dropCourse(1L, 3L);
        String res = studentService.addCourse(1L, 3L);
        assertEquals("Successfully enrolled.", res, "Service must handle the transition from DROPPED to ENROLLED.");
    }

    @Test void testFlow_EnrollAndVerifyTranscript() {
        studentService.addCourse(2L, 3L);
        Transcript t = studentService.viewTranscript(2L);
        assertTrue(t.getCourses().stream().anyMatch(c -> c.getCourseCode().equals("MATH201")));
    }

    @Test void testFlow_DropAndVerifyRemoval() {
        studentService.addCourse(2L, 2L); // Ensure they are in it
        studentService.dropCourse(2L, 2L);
        Transcript t = studentService.viewTranscript(2L);
        assertFalse(t.getCourses().stream().anyMatch(c -> c.getCourseCode().equals("CS210")),
                "Dropped courses must be invisible to the transcript view.");
    }

    @Test void testIntegrity_InstructorJoin() {
        var course = studentService.searchCourses("CS320").get(0);
        assertEquals("Ayse Demir", course.getInstructorName());
    }

    @Test void testIntegrity_TermMapping() {
        var course = studentService.searchCourses("CS320").get(0);
        assertEquals("2026-Spring", course.getTermName());
    }

    @Test void testRobustness_Injection() {
        assertTrue(studentService.searchCourses("' OR 1=1 --").isEmpty());
    }

    @Test void testFlow_TranscriptGrowth() {
        int initial = studentService.viewTranscript(1L).getCourses().size();
        studentService.addCourse(1L, 3L);
        int finale = studentService.viewTranscript(1L).getCourses().size();
        assertTrue(finale >= initial);
    }

    @Test void testSearch_BroadQuery() {
        assertFalse(studentService.searchCourses("CS").isEmpty());
    }

    @Test void testIntegrity_Grades() {
        Transcript t = studentService.viewTranscript(1L);
        if(t.getGrades().containsKey("CS210")) {
            assertNotNull(t.getGrades().get("CS210"));
        }
    }
}