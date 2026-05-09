package com.sis.admin.controller;

import com.sis.admin.api.AdminInterface;
import com.sis.admin.service.CourseManagementService;
import com.sis.admin.service.InstructorAssignmentService;
import com.sis.admin.service.UserManagementService;

public class AdminController implements AdminInterface {

    private final CourseManagementService courseService;
    private final InstructorAssignmentService assignmentService;
    private final UserManagementService userService;

    public AdminController(
            CourseManagementService courseService,
            InstructorAssignmentService assignmentService,
            UserManagementService userService) {
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @Override
    public String createCourse(String courseCode, String courseName, int quota) {
        if (courseCode == null || courseName == null || quota <= 0) {
            return "Error: Invalid course details provided.";
        }
        return courseService.createCourse(courseCode, courseName, quota);
    }

    @Override
    public String updateCourse(Long courseId, String newName, int newQuota) {
        if (courseId == null || newName == null || newQuota <= 0) {
            return "Error: Invalid update parameters.";
        }
        return courseService.updateCourse(courseId, newName, newQuota);
    }

    @Override
    public String assignInstructor(Long courseId, Long instructorId) {
        if (courseId == null || instructorId == null) {
            return "Error: Course ID and Instructor ID are required.";
        }
        return assignmentService.assignInstructor(courseId, instructorId);
    }

    @Override
    public String removeInstructor(Long courseId) {
        if (courseId == null) return "Error: Course ID is required.";
        return assignmentService.removeInstructor(courseId);
    }

    @Override
    public String createUser(String role, String firstName, String lastName, String email) {
        if (role == null || email == null) return "Error: Role and Email are required.";
        return userService.createUser(role, firstName, lastName, email);
    }

    @Override
    public String updateUser(Long userId, String firstName, String lastName) {
        if (userId == null) return "Error: User ID is required.";
        return userService.updateUser(userId, firstName, lastName);
    }

    @Override
    public String deactivateUser(Long userId) {
        if (userId == null) return "Error: User ID is required.";
        return userService.deactivateUser(userId);
    }

    @Override
    public String resetPassword(Long userId) {
        if (userId == null) return "Error: User ID is required.";
        return userService.resetPassword(userId);
    }
}