package com.sis.instructor.api;

import com.sis.instructor.model.RosterStudent;
import java.io.File;
import java.util.List;

public interface InstructorInterface {
    String uploadSyllabus(Long instructorId, Long courseId, File syllabusFile);
    List<RosterStudent> viewRoster(Long instructorId, Long courseId);
    String updateGrade(Long instructorId, Long studentId, Long courseId, String letterGrade);
}
