package com.sis.student.api;

import java.util.List; // This fix the red List error

public interface StudentServiceAPI {
    // API-style signatures based on instructor feedback
    List<Object> viewAvailableCourses();
    List<Object> searchCourses(String query);
    String addCourse(Long studentId, Long courseId);
    String dropCourse(Long studentId, Long courseId);
    Object viewTranscript(Long studentId);
    Object viewCourseDetails(Long courseId);
}