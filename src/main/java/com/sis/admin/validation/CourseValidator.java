package com.sis.admin.validation;

import com.sis.student.model.Course;
import java.util.List;

public class CourseValidator {

    public boolean isUniqueCourse(List<Course> database, String courseCode, String courseName) {
        for (Course course : database) {
            if (course.getCourseCode().equalsIgnoreCase(courseCode) ||
                    course.getCourseName().equalsIgnoreCase(courseName)) {
                return false;
            }
        }
        return true;
    }
}