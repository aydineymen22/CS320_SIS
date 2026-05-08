package com.sis.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceTest {
    private StudentService studentService;

    @BeforeEach
    void setUp() { studentService = new StudentService(); }

    @Test void testEnroll_Fail_AlreadyEnrolled() {
        studentService.addCourse(1L, 1L);
        String result = studentService.addCourse(1L, 1L);
        assertEquals("Error: Already enrolled in this section.", result);
    }

    @Test void testEnroll_Fail_SectionNotFound() {
        assertEquals("Error: Section not found.", studentService.addCourse(1L, 0L));
    }

    @Test void testDrop_Fail_EnrollmentNotFound() {
        assertEquals("Error: Enrollment not found.", studentService.dropCourse(99L, 99L));
    }

    @Test void testSearch_Empty() {
        assertTrue(studentService.searchCourses("NON_EXISTENT_NAME").isEmpty());
    }

    @Test void testSearch_CaseInsensitive() {
        assertEquals(studentService.searchCourses("cs320").size(),
                studentService.searchCourses("CS320").size());
    }

    @Test void testEnroll_Fail_InvalidStudent() {
        assertEquals("Error: Student or Section ID does not exist.", studentService.addCourse(8888L, 1L));
    }

    @Test void testTranscript_EmptyForNewStudent() {
        assertTrue(studentService.viewTranscript(7777L).getCourses().isEmpty());
    }

    @Test void testAvailableCourses_NotNull() {
        assertNotNull(studentService.viewAvailableCourses());
    }

    @Test void testEnroll_Fail_ClosedSection() {
        assertNotNull(studentService.addCourse(1L, 3L));
    }

    @Test void testDrop_Success_Message() {
        studentService.addCourse(1L, 2L);
        assertEquals("Successfully dropped.", studentService.dropCourse(1L, 2L));
    }
}