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

    private Course findCourseById(Long courseId) {
        for (Course c : mockCourseDatabase) {
            if (c.getCourseId().equals(courseId)) {
                return c;
            }
        }
        return null;
    }


    @Override
    public List<Course> viewAvailableCourses() {
        return mockCourseDatabase;
    }

    @Override
    public List<Course> searchCourses(String query) {
        List<Course> foundCourses = new ArrayList<>();

        for (Course course : mockCourseDatabase) {
            // Simple case-insensitive search logic
            if (course.getCourseName().toLowerCase().contains(query.toLowerCase()) ||
                    course.getCourseCode().toLowerCase().contains(query.toLowerCase())) {
                foundCourses.add(course);
            }
        }
        return foundCourses;
    }

    @Override
    public String addCourse(Long studentId, Long courseId) {
        Course course = findCourseById(courseId);
        if (course == null) return "Error: Course not found.";

        if (course.getQuota() <= 0) {
            return "Error: Course Quota Full.";
        }

        course.setQuota(course.getQuota() - 1);
        return "Successfully enrolled in " + course.getCourseName();
    }

    @Override
    public String dropCourse(Long studentId, Long courseId) {
        Course course = findCourseById(courseId);
        if (course == null) return "Error: Course not found.";

        course.setQuota(course.getQuota() + 1);
        return "Successfully dropped " + course.getCourseCode();
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