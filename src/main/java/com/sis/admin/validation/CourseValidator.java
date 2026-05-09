package com.sis.admin.validation;
import com.sis.admin.repository.CourseRepository;

public class CourseValidator {
    private final CourseRepository repository = new CourseRepository();

    public boolean isUniqueCourse(String courseCode, String courseName) {
        return !repository.existsByCodeOrName(courseCode, courseName);
    }
}