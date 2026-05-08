package com.sis.student.model;

import java.util.List;
import java.util.Map;

public class Transcript {
    private Long studentId;
    private List<Course> courses;
    private Map<String, String> grades; // Key: CourseCode, Value: Grade

    public Transcript(Long studentId, List<Course> courses, Map<String, String> grades) {
        this.studentId = studentId;
        this.courses = courses;
        this.grades = grades;
    }

    public List<Course> getCourses() { return courses; }
    public Map<String, String> getGrades() { return grades; }
}