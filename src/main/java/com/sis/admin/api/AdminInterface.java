package com.sis.admin.api;

public interface AdminInterface {
    String createCourse(String courseCode, String courseName, int quota);
    String updateCourse(Long courseId, String newName, int newQuota);
    String assignInstructor(Long courseId, Long instructorId);
    String removeInstructor(Long courseId);
    String createUser(String role, String firstName, String lastName, String email);
    String updateUser(Long userId, String firstName, String lastName);
    String deactivateUser(Long userId);
    String resetPassword(Long userId);
}