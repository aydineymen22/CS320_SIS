package com.sis.admin.service;

import com.sis.admin.repository.CourseRepository;

public class InstructorAssignmentService {
    private final CourseRepository courseRepository;

    public InstructorAssignmentService() {
        this.courseRepository = new CourseRepository();
    }

    public String assignInstructor(Long sectionId, Long instructorId) {
        boolean success = courseRepository.assignInstructor(sectionId, instructorId);
        return success ? "Success: Instructor ID " + instructorId + " assigned to Section ID " + sectionId + "." : "Error: Assignment failed. Check if IDs exist.";
    }

    public String removeInstructor(Long sectionId) {
        boolean success = courseRepository.removeInstructor(sectionId);
        return success ? "Success: Instructor removed from Section ID " + sectionId + "." : "Error: Removal failed.";
    }
}