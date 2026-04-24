package com.sis.student.service;

import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class StudentServiceTest {
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
        // Here we add "Mock" data directly into the service for testing
        Course fullCourse = new Course(101L, "CS320", "Software Engineering", 0, "Dr. Smith");
        Course availableCourse = new Course(102L, "CS101", "Intro to CS", 5, "Dr. Jones");

        // Manual injection of test data
        studentService.viewAvailableCourses().addAll(List.of(fullCourse, availableCourse));
    }

    @Test
    void testAddCourse_WhenQuotaIsFull_ShouldReturnError() {
        // [cite: 1734] This is our test case:
        // Input: student 1, full course 101
        String result = studentService.addCourse(1L, 101L);

        // Expected outcome: "Error: Course Quota Full."
        assertEquals("Error: Course Quota Full.", result);
    }

    @Test
    void testAddCourse_WhenSuccessful_ShouldDecreaseQuota() {
        studentService.addCourse(1L, 102L);
        assertEquals(4, studentService.viewAvailableCourses().get(1).getQuota());
    }

    @Test
    void testSearchCourses_ShouldReturnMatchingResults() {
        // 1. Act: Search for "Software"
        List<Course> results = studentService.searchCourses("Software");

        // 2. Assert: We expect to find 1 course (CS320)
        assertEquals(1, results.size());
        assertEquals("Software Engineering", results.get(0).getCourseName());
    }

    @Test
    void testDropCourse_ShouldIncreaseQuota() {
        // 1. Arrange: Ensure a course has been "joined" first
        studentService.addCourse(1L, 102L); // Starting quota was 5, now 4

        // 2. Act: Drop the course
        String result = studentService.dropCourse(1L, 102L);

        // 3. Assert: Verify success message and that quota returned to 5
        assertEquals("Successfully dropped CS101", result);
        assertEquals(5, studentService.viewAvailableCourses().get(1).getQuota());
    }

    @Test
    void testViewTranscript_ShouldReturnStudentRecords() {
        // 1. Arrange: Enroll the student in a course first
        studentService.addCourse(1L, 102L);

        // 2. Act: Retrieve the transcript
        Transcript transcript = studentService.viewTranscript(1L);

        // 3. Assert: Verify the transcript contains the correct course
        assertNotNull(transcript);
        assertEquals(1, transcript.getCourses().size());
        assertEquals("CS101", transcript.getCourses().get(0).getCourseCode());
    }

    @Test
    void testViewCourseDetails_ShouldReturnCorrectCourse() {
        // 1. Act: Request details for existing course 101
        Course details = studentService.viewCourseDetails(101L);

        // 2. Assert: Verify the returned object matches the expected course
        assertNotNull(details);
        assertEquals("CS320", details.getCourseCode());
        assertEquals("Software Engineering", details.getCourseName());
    }


}