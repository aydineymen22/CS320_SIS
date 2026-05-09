package com.sis.admin.service;

import com.sis.admin.repository.CourseRepository;
import com.sis.admin.validation.CourseValidator;
import java.sql.SQLException;

public class CourseManagementService {
    private final CourseValidator courseValidator;
    private final CourseRepository courseRepository;

    public CourseManagementService(CourseValidator courseValidator) {
        this.courseValidator = courseValidator;
        this.courseRepository = new CourseRepository();
    }

    public String createCourse(String courseCode, String courseName, int quota) {
        if (isBlank(courseCode) || isBlank(courseName) || quota <= 0) {
            return "Error: Invalid course details provided.";
        }

        if (!courseValidator.isUniqueCourse(courseCode, courseName)) {
            return "Error: A course with code '" + courseCode + "' or name '" + courseName + "' already exists.";
        }

        try {
            Long newSectionId = courseRepository.saveCourse(courseCode, courseName, quota);
            return "Success: Course '" + courseName + "' created and assigned Section ID: " + newSectionId;
        } catch (SQLException e) {
            return "Database Error: Could not create course. " + e.getMessage();
        }
    }

    public String updateCourse(Long courseId, String newName, int newQuota) {
        if (courseId == null || isBlank(newName) || newQuota <= 0) {
            return "Error: Invalid update parameters.";
        }

        boolean updated = courseRepository.updateCourseDetails(courseId, newName, newQuota);
        return updated
                ? "Success: Course details updated. Name: " + newName + ", quota: " + newQuota + "."
                : "Error: Course not found or update failed.";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
