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
        boolean updated = courseRepository.updateCourseQuota(courseId, newQuota);
        return updated ? "Success: Course quota updated to " + newQuota + "." : "Error: Course not found or update failed.";
    }
}
