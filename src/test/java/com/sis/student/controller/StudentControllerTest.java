package com.sis.student.controller;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StudentControllerTest {
    private StudentController controller;

    @BeforeEach
    void setUp() {
        // We initialize the gatekeeper with its brain
        StudentServiceAPI service = new StudentService();
        controller = new StudentController(service);
    }

    @Test
    void testEnrollInCourse_WithNullInput_ShouldReturnError() {
        // This test ensures the gatekeeper prevents improper system alterations
        String result = controller.enrollInCourse(null, 101L);
        assertEquals("Error: Invalid ID provided.", result);
    }
}