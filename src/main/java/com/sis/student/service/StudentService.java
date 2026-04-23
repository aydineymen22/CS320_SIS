package com.sis.student.service;

import com.sis.student.api.StudentServiceAPI;
import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import java.util.ArrayList;
import java.util.List;

public class StudentService implements StudentServiceAPI {

    // These would normally come from a Database (Repository Layer)
    // For now, we use an empty list as a placeholder
    private List<Course> mockCourseDatabase = new ArrayList<>();

    @Override
    public List<Course> viewAvailableCourses() {
        return mockCourseDatabase;
    }

    @Override
    public List<Course> searchCourses(String query) {
        // Logic for searching will go here
        return new ArrayList<>();
    }

    @Override
    public String addCourse(Long studentId, Long courseId) {
// 1. Find the course in our "database"
        Course courseToJoin = null;
        for (Course c : mockCourseDatabase) {
            if (c.getCourseId().equals(courseId)) {
                courseToJoin = c;
                break;
            }
        }

        // 2. Check if the course exists
        if (courseToJoin == null) {
            return "Error: Course not found.";
        }

        // 3. APPLY BUSINESS RULE: Check the Quota (SRS-SIS-002)
        if (courseToJoin.getQuota() <= 0) {
            return "Error: Course Quota Full."; // This satisfies SRS-NFR-005
        }

        // 4. Success logic
        courseToJoin.setQuota(courseToJoin.getQuota() - 1);
        return "Successfully enrolled in " + courseToJoin.getCourseName();

    }

    @Override
    public String dropCourse(Long studentId, Long courseId) {
        return "Not implemented yet";
    }

    @Override
    public Transcript viewTranscript(Long studentId) {
        return null;
    }

    @Override
    public Course viewCourseDetails(Long courseId) {
        return null;
    }
}