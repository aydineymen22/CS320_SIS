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

    public List<Course> getAvailableCourses() { return studentService.viewAvailableCourses(); }
    public List<Course> search(String query) { return studentService.searchCourses(query); }
    public String enrollInCourse(Long studentId, Long sectionId) { return studentService.addCourse(studentId, sectionId); }
    public String withdrawFromCourse(Long studentId, Long sectionId) { return studentService.dropCourse(studentId, sectionId); }
    public Transcript getTranscript(Long studentId) { return studentService.viewTranscript(studentId); }
}