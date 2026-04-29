package com.sis.admin.service;

public class InstructorAssignmentService {

    public InstructorAssignmentService() {
        // Dependencies like CourseRepository will be injected here later
    }

    public String assignInstructor(Long courseId, Long instructorId) {
        // TODO: Check if course exists, check if instructor exists
        // TODO: Safely unlink old instructor if one exists (1-to-1 mapping rule)
        // TODO: Link new instructor and save

        return "Success: Instructor ID " + instructorId + " has been assigned to Course ID " + courseId + ".";
    }

    public String removeInstructor(Long courseId) {
        // TODO: Unlink instructor from course and save

        return "Success: Instructor removed from Course ID " + courseId + ".";
    }
}