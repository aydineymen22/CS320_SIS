package com.sis.instructor.api;

import java.io.File;
import java.util.List;

public interface InstructorInterface {
    String uploadSyllabus(Long instructorId, Long courseId, File syllabusFile);
    List<String> viewRoster(Long instructorId, Long courseId);
    String updateGrade(Long instructorId, Long studentId, Long courseId, String letterGrade);
}
