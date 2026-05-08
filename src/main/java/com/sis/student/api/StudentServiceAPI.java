package com.sis.student.api;

import com.sis.common.model.Course;
import com.sis.common.model.Transcript;
import java.util.List;

public interface StudentServiceAPI {
    List<Course> viewAvailableCourses();
    List<Course> searchCourses(String query);
    String addCourse(Long studentId, Long sectionId);
    String dropCourse(Long studentId, Long sectionId);
    Transcript viewTranscript(Long studentId);
}