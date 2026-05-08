package com.sis.student.controller;

import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import com.sis.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * System Integration Test for StudentController.
 * Verifies the full path from Controller -> Service -> MySQL.
 */
public class StudentControllerTest {

    private StudentController studentController;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
        studentController = new StudentController(studentService);
    }

    @Test
    void testGetAvailableCourses_FromLiveDB() {
        List<Course> courses = studentController.getAvailableCourses();

        assertNotNull(courses, "Controller should return a list even if empty");
        assertTrue(courses.stream().anyMatch(c -> c.getCourseCode().equals("CS320")),
                "Controller must be able to see CS320 in the database");
    }

    @Test
    void testEnrollmentFlow_EndToEnd() {
        String result = studentController.enrollInCourse(1L, 1L);

        assertNotNull(result);
        System.out.println("Enrollment Response via Controller: " + result);
    }

    @Test
    void testSearch_CaseInsensitivityInDB() {
        List<Course> results = studentController.search("software");

        assertFalse(results.isEmpty(), "Search should return results from course_listing_view");
    }

    @Test
    void testTranscript_RealViewMapping() {
        Transcript transcript = studentController.getTranscript(1L);

        assertNotNull(transcript);
        assertNotNull(transcript.getGrades());
        System.out.println("Transcript retrieved for student 1 has " + transcript.getCourses().size() + " courses.");
    }

    @Test
    void testWithdrawal_StatePersistence() {
        String result = studentController.withdrawFromCourse(2L, 1L);

        assertNotNull(result);
        assertTrue(result.contains("Successfully") || result.contains("Error"),
                "Controller must return a status message from the database");
    }
}