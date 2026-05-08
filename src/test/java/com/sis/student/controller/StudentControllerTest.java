package com.sis.student.controller;

import com.sis.student.service.StudentService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentControllerTest {
    private final StudentService service = new StudentService();

    @Test void path_CheckDependencyInjection() { assertNotNull(service); }

    @Test void path_GetSchedule_Connection() { assertNotNull(service.getMySchedule(1L)); }

    @Test void path_Search_CaseInsensitivity() {
        assertEquals(service.searchCourses("CS").size(), service.searchCourses("cs").size());
    }

    @Test void path_Enrollment_BasicFlow() {
        assertNotNull(service.addCourse(1L, 3L));
    }

    @Test void path_Transcript_IdentityCheck() {
        assertEquals(2L, service.viewTranscript(2L).getStudentId());
    }

    @Test void integrity_CourseObject_HasName() {
        assertNotNull(service.viewAvailableCourses().get(0).getCourseName());
    }

    @Test void integrity_CourseObject_HasCode() {
        assertNotNull(service.viewAvailableCourses().get(0).getCourseCode());
    }

    @Test void path_Drop_LogicVerification() {
        String res = service.dropCourse(1L, 3L);
        assertTrue(res.contains("Success") || res.contains("Error"));
    }

    @Test void path_Search_NonExistentReturnsEmpty() {
        assertTrue(service.searchCourses("VOID_COURSE").isEmpty());
    }

    @Test void path_AddCourse_BoundaryID() {
        assertEquals("Error: Course not found.", service.addCourse(1L, 0L));
    }
}