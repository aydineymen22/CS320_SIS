package com.sis.student.controller;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
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

    public List<Course> getAvailableCourses() {
        return studentService.viewAvailableCourses();
    }

    public List<Course> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAvailableCourses();
        }
        return studentService.searchCourses(query);
    }

    public Transcript getTranscript(Long studentId) {
        if (studentId == null) return null;
        return studentService.viewTranscript(studentId);
    }

    public String withdrawFromCourse(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            return "Error: Invalid ID provided.";
        }
        return studentService.dropCourse(studentId, courseId);
    }
}