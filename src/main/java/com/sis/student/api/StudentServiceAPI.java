package com.sis.student.api;

import com.sis.student.model.Course;
import com.sis.student.model.Transcript;
import java.util.List;

public interface StudentServiceAPI {
    List<Course> viewAvailableCourses();
    List<Course> searchCourses(String query);
    String addCourse(Long studentId, Long courseId);
    String dropCourse(Long studentId, Long courseId);
    Transcript viewTranscript(Long studentId);
    Course viewCourseDetails(Long courseId);
}