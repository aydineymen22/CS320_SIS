package com.sis.student.model;

import java.util.List;
import java.util.Map;

public class Transcript {
    private Long studentId;
    private List<Course> courses; // List of courses taken
    private Map<String, String> grades; // Course code -> Grade (e.g., "CS320" -> "AA")

    public Transcript(Long studentId, List<Course> courses, Map<String, String> grades) {
        this.studentId = studentId;
        this.courses = courses;
        this.grades = grades;
    }

    // Getters
    public Long getStudentId() { return studentId; }
    public List<Course> getCourses() { return courses; }
    public Map<String, String> getGrades() { return grades; }
}