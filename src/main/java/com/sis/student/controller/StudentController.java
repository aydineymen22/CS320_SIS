package com.sis.student.controller;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.model.Course;
import java.util.List;

public class StudentController {
    private final StudentServiceAPI studentService;

    public StudentController(StudentServiceAPI studentService) {
        this.studentService = studentService;
    }

    public String enrollInCourse(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            return "Error: Invalid ID provided.";
        }
        return studentService.addCourse(studentId, courseId);
    }

    public List<Course> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return studentService.viewAvailableCourses();
        }
        return studentService.searchCourses(query);
    }

    public String withdrawFromCourse(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            return "Error: Invalid ID provided.";
        }
        return studentService.dropCourse(studentId, courseId);
    }
}