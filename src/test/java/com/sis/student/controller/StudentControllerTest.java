package com.sis.student.controller;

import com.sis.student.service.StudentService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentControllerTest {
    private final StudentService service = new StudentService();

    @Test void verifyServiceAccessibility() { assertNotNull(service); }
    @Test void testPath_ViewCourses() { assertNotNull(service.viewAvailableCourses()); }
    @Test void testPath_EnrollmentLogic() {
        // Simulates the path a 'doPost' call would take
        String res = service.addCourse(1L, 2L);
        assertNotNull(res);
    }
    @Test void testPath_DropLogic() {
        String res = service.dropCourse(1L, 2L);
        assertNotNull(res);
    }
    @Test void testPath_Search() { assertFalse(service.searchCourses("CS").isEmpty()); }
    @Test void testPath_Transcript() { assertNotNull(service.viewTranscript(1L)); }
    @Test void verifyDataIntegrity_CourseCode() {
        var courses = service.searchCourses("CS320");
        if(!courses.isEmpty()) assertEquals("CS320", courses.get(0).getCourseCode());
    }
    @Test void verifyDataIntegrity_QuotaType() {
        var courses = service.viewAvailableCourses();
        if(!courses.isEmpty()) assertTrue(courses.get(0).getAvailableQuota() >= 0);
    }
    @Test void testPath_MultipleEnrollment() {
        service.addCourse(1L, 3L);
        String res = service.addCourse(1L, 3L);
        assertTrue(res.contains("Error") || res.contains("Already"));
    }
    @Test void testPath_EmptySearch() {
        assertEquals(service.viewAvailableCourses().size(), service.searchCourses("").size());
    }
}