package com.sis.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transcript {
    private Long studentId;
    private List<Course> courses;
    private Map<String, String> grades; // Key: CourseCode, Value: Grade

    public Transcript(Long studentId) {
        this.studentId = studentId;
        this.courses = new ArrayList<>();
        this.grades = new HashMap<>();
    }

    public void addCourse(Course course, String grade) {
        this.courses.add(course);
        this.grades.put(course.getCourseCode(), grade);
    }

    public Long getStudentId() { return studentId; }
    public List<Course> getCourses() { return courses; }
    public Map<String, String> getGrades() { return grades; }
}