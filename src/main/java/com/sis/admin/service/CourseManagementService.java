package com.sis.admin.service;

import com.sis.admin.validation.CourseValidator;
import com.sis.student.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseManagementService {
    private final CourseValidator courseValidator;

    // In-memory mock database matching the Student component style
    private List<Course> mockCourseDatabase = new ArrayList<>();
    private Long courseIdCounter = 1000L;

    public CourseManagementService(CourseValidator courseValidator) {
        this.courseValidator = courseValidator;
    }

    public String createCourse(String courseCode, String courseName, int quota) {
        if (!courseValidator.isUniqueCourse(mockCourseDatabase, courseCode, courseName)) {
            return "Error: A course with this code or name already exists.";
        }

        Long newId = courseIdCounter++;
        Course newCourse = new Course(newId, courseCode, courseName, quota, "Unassigned");
        mockCourseDatabase.add(newCourse);

        return "Success: Course '" + courseName + "' created with ID: " + newId;
    }

    public String updateCourse(Long courseId, String newName, int newQuota) {
        for (Course course : mockCourseDatabase) {
            if (course.getCourseId().equals(courseId)) {

                course.setQuota(newQuota);
                return "Success: Course updated successfully.";
            }
        }
        return "Error: Course not found.";
    }
}