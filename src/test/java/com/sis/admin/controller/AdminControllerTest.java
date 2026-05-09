package com.sis.admin.controller;

import com.sis.admin.service.CourseManagementService;
import com.sis.admin.service.InstructorAssignmentService;
import com.sis.admin.service.UserManagementService;
import com.sis.admin.validation.CourseValidator;
import com.sis.admin.validation.SecurityService;
import com.sis.admin.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminControllerTest {

    private AdminController controller;

    @BeforeEach
    void setUp() {
        CourseValidator courseValidator = new CourseValidator();
        UserValidator userValidator = new UserValidator();
        SecurityService securityService = new SecurityService();

        CourseManagementService courseService = new CourseManagementService(courseValidator);
        InstructorAssignmentService assignmentService = new InstructorAssignmentService();
        UserManagementService userService = new UserManagementService(userValidator, securityService);

        controller = new AdminController(courseService, assignmentService, userService);
    }

    @Test
    void testCreateCourse_WithNullCode_IsBlockedByController() {
        String result = controller.createCourse(null, "Test Course", 50);

        assertTrue(result.startsWith("Error"));
        assertTrue(result.contains("Invalid course details provided"));
    }

    @Test
    void testCreateCourse_WithNegativeQuota_IsBlockedByController() {
        String result = controller.createCourse("CS101", "Intro", -10);

        assertTrue(result.startsWith("Error"));
        assertTrue(result.contains("Invalid course details"));
    }

    @Test
    void testCreateUser_WithNullRole_IsBlockedByController() {
        String result = controller.createUser(null, "John", "Doe", "john@ozu.edu.tr");

        assertTrue(result.startsWith("Error"));
        assertTrue(result.contains("Role, Name, and Email are required"));
    }

    @Test
    void testUpdateCourse_WithNullId_IsBlockedByController() {
        String result = controller.updateCourse(null, "New Name", 30);

        assertTrue(result.startsWith("Error"));
    }

    @Test
    void testAssignInstructor_WithNullIds_IsBlockedByController() {
        String result = controller.assignInstructor(null, null);

        assertTrue(result.startsWith("Error"));
        assertTrue(result.contains("Course ID and Instructor ID are required"));
    }
}
